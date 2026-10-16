# 模块与依赖

`api` 定义 OpenAI 兼容请求/响应、错误模型与模型管理契约；`biz` 实现模型资料、用量、协议客户端注册、请求转换和 Controller；`startup` 组装启动类、协议错误过滤与异常映射。模块方向固定为 `startup -> biz -> api`。
