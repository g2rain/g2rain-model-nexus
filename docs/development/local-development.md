# 本地开发

环境：JDK 25、Maven、MySQL 8.0.13+、Redis、Nacos 和已授权的模型服务。初始化脚本为 `scripts/g2rain-model-nexus.sql`，仅用于独立开发库。

```bash
mvn clean verify
mvn -pl g2rain-model-nexus-startup -am spring-boot:run -Dspring-boot.run.profiles=dev
```

默认端口 `8080`，默认 profile 为 `nacos`；本地 `dev` 与 `nacos` 不应同时启用。健康检查为 `GET /actuator/health`。
