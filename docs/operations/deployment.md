# 构建与部署

```bash
mvn clean verify
./build.sh <tag>
```

部署前确认 Nacos、MySQL、Redis、Gateway 路由、模型供应商网络与凭据、服务身份和密钥轮换策略。`build.sh` 使用 Jib 构建本地镜像；发布前应验证 `/v1` 兼容接口和关键模型协议。
