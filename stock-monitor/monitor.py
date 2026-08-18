#!/usr/bin/env python3
"""
A-share Intraday Stock Monitor
触发线监控 + WxPusher 微信推送
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

def fetch_price(ticker):
    env_key = f"PRICE_{ticker.replace('.', '_')}"
    price = os.getenv(env_key)
    return float(price) if price else None

def send_wxpusher(cfg, title, content):
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

def check_trigger(cfg, ticker, name, price, upper, lower):
    if price is None:
        return None
    if price >= upper:
        return {
            "direction": "UPPER",
            "trigger_price": upper,
            "current_price": price,
            "msg": f"🚨 {name}({ticker}) 上破触发线！现价 {price} ≥ {upper}"
        }
    if price <= lower:
        return {
            "direction": "LOWER",
            "trigger_price": lower,
            "current_price": price,
            "msg": f"🚨 {name}({ticker}) 下破触发线！现价 {price} ≤ {lower}"
        }
    return None

def main():
    cfg = load_config()

    if os.getenv("WXPUSHER_APP_TOKEN"):
        cfg["wxpusher"]["app_token"] = os.getenv("WXPUSHER_APP_TOKEN")
    if os.getenv("WXPUSHER_UIDS"):
        cfg["wxpusher"]["uids"] = os.getenv("WXPUSHER_UIDS").split(",")

    if not is_trading_time(cfg):
        print("非交易时间，静默退出。")
        sys.exit(0)

    triggered = []
    for stock in cfg["stocks"]:
        ticker = stock["ticker"]
        price = fetch_price(ticker)

        result = check_trigger(
            cfg, ticker, stock["name"], price,
            stock["upper_trigger"], stock["lower_trigger"]
        )
        if result:
            triggered.append(result)

    if not triggered:
        print("未命中任何触发线，静默。")
        sys.exit(0)

    title = f"【盘中触发】{len(triggered)} 只股票命中"
    content_lines = [f"触发时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}", ""]
    for t in triggered:
        content_lines.append(t["msg"])
    content = "\n".join(content_lines)

    print(title)
    print(content)

    success = send_wxpusher(cfg, title, content)
    if not success:
        print("推送失败，请检查 WxPusher 配置。")
        sys.exit(1)
    print("推送成功。")

if __name__ == "__main__":
    main()
