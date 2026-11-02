# 测试策略

覆盖模型协议路由、请求/响应转换、工具调用转换、Embeddings 维度、错误映射、用量记录及租户访问。使用集成测试验证供应商超时、认证失败、非法模型和 `/v1` 兼容响应。完整命令为 `mvn clean verify`。2026-11-01 已运行该命令，但在 Biz 编译阶段因当前 `g2rain-common 1.0.9` 缺少 `JSON.str2map`、`Strings.defaultIfBlank` 与 `Strings.blankToNull` 而失败，测试未开始执行。
