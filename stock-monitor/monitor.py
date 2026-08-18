#!/usr/bin/env python3
"""
A-share Intraday Stock Monitor
触发线监控 + WxPusher 微信推送
数据源: 新浪财经免费接口 (qt.gtimg.cn)
"""
import json
import sys
import os
import requests
from datetime import datetime, time

CONFIG_PATH = os.path.join(os.path.dirname(__file__), "config.json")

def load_config():
    with open(CONFIG_PATH, "r", encoding="utf-8") as f:
        return json.load(f)

def is_trading_time(cfg):
    """检查当前是否在交易时间内（仅工作日）"""
    now = datetime.now()
    if now.weekday() >= 5:
        return False
    t = now.time()
    morning = (
        time.fromisoformat(cfg["trading_hours"]["morning_start"])
        <= t <=
        time.fromisoformat(cfg["trading_hours"]["morning_end"])
    )
    afternoon = (
        time.fromisoformat(cfg["trading_hours"]["afternoon_start"])
        <= t <=
        time.fromisoformat(cfg["trading_hours"]["afternoon_end"])
    )
    return morning or afternoon

def fetch_prices(tickers):
    """
    通过新浪财经免费接口获取实时行情
    tickers: ["688519.SH", "603039.SH"]
    返回: {"688519.SH": {"price": 311.50, "name": "南亚新材", "high": 318.80, "low": 305.21, "volume": 5148363, "amount": 160431, "pre_close": 315.50, "open": 318.00}, ...}
    """
    # 转换代码格式: 688519.SH -> sh688519
    code_map = {}
    query_codes = []
    for t in tickers:
        code = t.split(".")[0]
        prefix = "sh" if code.startswith("6") or code.startswith("688") or code.startswith("689") else "sz"
        qcode = f"{prefix}{code}"
        query_codes.append(qcode)
        code_map[qcode] = t

    url = f"https://qt.gtimg.cn/q={','.join(query_codes)}"
    try:
        r = requests.get(url, timeout=15)
        r.raise_for_status()
    except Exception as e:
        print(f"[Error] 获取行情失败: {e}")
        return {}

    results = {}
    for line in r.text.strip().split(";"):
        line = line.strip()
        if not line or not line.startswith("v_"):
            continue
        parts = line.split('="')
        if len(parts) != 2:
            continue
        qcode = parts[0].replace("v_", "")
        data = parts[1].strip('"').split("~")
        if len(data) < 40:
            continue

        original_ticker = code_map.get(qcode)
        if not original_ticker:
            continue

        try:
            results[original_ticker] = {
                "name": data[1],
                "code": data[2],
                "price": float(data[3]),
                "pre_close": float(data[4]),
                "open": float(data[5]),
                "high": float(data[33]),
                "low": float(data[34]),
                "volume": int(data[36]) if data[36] else 0,      # 成交量(手)
                "amount": int(data[37]) if data[37] else 0,      # 成交额(万元)
                "turnover": float(data[38]) if data[38] else 0,  # 换手率%
                "change_pct": float(data[32]) if data[32] else 0, # 涨跌幅%
                "time": data[30] if len(data) > 30 else ""       # 数据时间
            }
        except (ValueError, IndexError) as e:
            print(f"[Warn] 解析 {qcode} 数据失败: {e}")
            continue

    return results

def send_wxpusher(cfg, title, content):
    """推送消息到微信"""
    wx = cfg["wxpusher"]
    payload = {
        "appToken": wx["app_token"],
        "uids": wx["uids"],
        "contentType": wx["content_type"],
        "summary": title,
        "content": content
    }
    try:
        r = requests.post(
            "https://wxpusher.zjiecode.com/api/send/message",
            json=payload,
            timeout=15
        )
        return r.status_code == 200
    except Exception as e:
        print(f"[WxPusher Error] {e}")
        return False

def check_trigger(stock_cfg, price_data):
    """检查是否命中触发线，返回分析结果"""
    ticker = stock_cfg["ticker"]
    name = stock_cfg["name"]
    upper = stock_cfg["upper_trigger"]
    lower = stock_cfg["lower_trigger"]

    if not price_data:
        return None

    price = price_data["price"]
    high = price_data["high"]
    low = price_data["low"]
    volume = price_data["volume"]
    amount = price_data["amount"]
    change_pct = price_data["change_pct"]
    pre_close = price_data["pre_close"]

    direction = None
    trigger_price = None

    if price >= upper:
        direction = "UPPER"
        trigger_price = upper
    elif price <= lower:
        direction = "LOWER"
        trigger_price = lower
    else:
        return None

    # 盘面简评
    reviews = []
    if change_pct > 0:
        reviews.append(f"涨{change_pct:.2f}%")
    elif change_pct < 0:
        reviews.append(f"跌{abs(change_pct):.2f}%")
    else:
        reviews.append("平盘")

    # 量能判断
    if volume > 500000:
        reviews.append("量能活跃")
    elif volume > 100000:
        reviews.append("量能中等")
    else:
        reviews.append("量能萎缩")

    # 日内振幅
    amplitude = ((high - low) / pre_close) * 100 if pre_close else 0
    if amplitude > 5:
        reviews.append(f"振幅较大({amplitude:.1f}%)")

    review = "，".join(reviews)

    return {
        "ticker": ticker,
        "name": name,
        "direction": direction,
        "trigger_price": trigger_price,
        "current_price": price,
        "high": high,
        "low": low,
        "volume": volume,
        "amount": amount,
        "change_pct": change_pct,
        "review": review,
        "msg": f"🚨 {name}({ticker}) {'上破' if direction == 'UPPER' else '下破'}触发线！\n现价: {price} {'≥' if direction == 'UPPER' else '≤'} {trigger_price}\n最高: {high} | 最低: {low} | 涨跌: {change_pct:+.2f}%\n成交: {volume:,}手 / {amount:,}万元\n盘面: {review}"
    }

def main():
    cfg = load_config()

    # 环境变量可覆盖配置
    if os.getenv("WXPUSHER_APP_TOKEN"):
        cfg["wxpusher"]["app_token"] = os.getenv("WXPUSHER_APP_TOKEN")
    if os.getenv("WXPUSHER_UIDS"):
        cfg["wxpusher"]["uids"] = os.getenv("WXPUSHER_UIDS").split(",")

    # 非交易日/非交易时间静默退出
    if not is_trading_time(cfg):
        print("非交易时间，静默退出。")
        sys.exit(0)

    # 获取所有监控股票的价格
    tickers = [s["ticker"] for s in cfg["stocks"]]
    prices = fetch_prices(tickers)

    if not prices:
        print("获取行情失败，静默退出。")
        sys.exit(0)

    # 检查触发
    triggered = []
    for stock in cfg["stocks"]:
        ticker = stock["ticker"]
        price_data = prices.get(ticker)
        result = check_trigger(stock, price_data)
        if result:
            triggered.append(result)

    if not triggered:
        print("未命中任何触发线，静默。")
        sys.exit(0)

    # 构建推送内容
    title = f"【盘中触发】{len(triggered)} 只股票命中"
    content_lines = [
        f"触发时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
        ""
    ]
    for t in triggered:
        content_lines.append(t["msg"])
        content_lines.append("")

    content = "\n".join(content_lines)

    print("=" * 50)
    print(title)
    print("=" * 50)
    print(content)

    success = send_wxpusher(cfg, title, content)
    if not success:
        print("\n推送失败，请检查 WxPusher 配置。")
        sys.exit(1)
    print("\n✅ 推送成功。")

if __name__ == "__main__":
    main()
