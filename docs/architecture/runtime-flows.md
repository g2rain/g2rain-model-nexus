# 运行流程

1. 调用方提交 OpenAI 风格的聊天或 Embeddings 请求。
2. 服务定位模型资料，`ModelClientRegistry` 按存储的协议选择客户端。
3. 请求在 OpenAI 模型和 Spring AI 模型之间转换，调用供应商并映射响应、工具调用或用量。
4. `/v1/**` 的协议错误由专用过滤器和异常映射输出 OpenAI 风格错误响应；上游厂商错误（`UpstreamException`）原样返回上游 HTTP 状态码与厂商错误码，原文脱敏密钥与地址，识别不了的仍为 `REMOTE_SERVICE_ERROR`。
