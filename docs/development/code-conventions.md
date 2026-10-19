# 代码规范

- 保持 `startup -> biz -> api` 和 Controller -> Service -> DAO。
- API 只暴露稳定的 OpenAI 兼容和模型查询契约，不泄露 PO、供应商密钥或 Biz DTO。
- 协议转换、错误码和模型路由是兼容边界，变更必须评估调用方影响。
- 模型资料、用量、配置和协议变化需同步文档与测试。
