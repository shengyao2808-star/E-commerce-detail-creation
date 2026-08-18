# Stock Monitor 盘中触发监控

## 功能
- 监控 A 股实时价格，命中触发线时自动推送微信提醒（WxPusher）
- 当前监控标的：
  - 南亚新材 (688519) — 上破 324 / 下破 305
  - 泛微网络 (603039) — 上破 49.10 / 下破 42.60

## 部署说明

### 1. 配置 GitHub Secrets
在仓库 Settings → Secrets and variables → Actions 中添加：
- `WXPUSHER_APP_TOKEN`: WxPusher 的 appToken
- `WXPUSHER_UIDS`: WxPusher 的 UID（多个用逗号分隔）

### 2. 接入真实数据源（重要）
当前脚本中的价格获取为占位实现。实际运行需接入以下任一数据源：
- **iFinD**: 安装 `agent-gw` SDK，调用 `ifind_get_stock_realtime_price`
- **Wind**: 使用 Wind 量化接口获取实时行情
- **其他**: 东财、同花顺等免费接口（注意频率限制）

### 3. 定时任务
GitHub Actions 已配置为工作日交易时间内每 5 分钟运行一次。
- 上午：09:30–11:30
- 下午：13:00–15:00

### 4. 本地测试
```bash
cd stock-monitor
pip install -r requirements.txt
# 注入测试价格
export PRICE_688519_SH=325.0
export PRICE_603039_SH=43.0
python monitor.py
```
