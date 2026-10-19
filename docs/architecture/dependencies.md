# 依赖边界

API 不依赖 Biz/Startup；Biz 使用 Spring AI 和协议客户端实现路由，不反向依赖 Startup；Startup 只负责运行时装配。其他服务消费 `/v1` 或公开查询契约，不得直接依赖本项目 PO 或模型密钥数据。
