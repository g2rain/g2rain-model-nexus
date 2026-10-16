# 配置说明

入口为 `g2rain-model-nexus-startup/src/main/resources/application.yml`。

| 配置 | 说明 |
| --- | --- |
| `SERVER_PORT` | HTTP 端口，默认 `8080`。 |
| `SPRING_PROFILES_ACTIVE` | 默认 `nacos`；本地可选 `dev`。 |
| `NACOS_SERVER_ADDR` | Nacos 注册与配置中心地址。 |
| `g2rain.model-nexus.protocol-error-path-patterns` | 使用 OpenAI 协议错误映射的路径，当前含 `/v1/**`。 |

模型供应商 URL、密钥和接入参数由受控配置提供，禁止进入仓库或文档。
