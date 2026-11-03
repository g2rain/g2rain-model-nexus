# 构建、镜像与部署

## 构建与镜像边界

| 操作 | 命令或位置 | 已覆盖的范围 | 不代表 |
| --- | --- | --- | --- |
| 本地 Maven 验证 | `mvn clean verify` | Maven 验证生命周期与仓库测试 | 容器已构建、已推送或已部署。 |
| 本地容器构建 | `./build.sh 1.0.0` | 完整 Maven 构建与测试后，Jib 使用同一配置将镜像载入本地 Docker | 镜像已推送、可匿名拉取或在环境中可运行；需要 Docker daemon。 |
| CI 构建与测试 | tag 工作流根目录 `mvn --batch-mode --no-transfer-progress clean install` | 完整 Maven 构建与测试 | 实际部署、在线演示或匿名拉取。 |
| GHCR 推送 | 同一 CI 的 Jib `push` | `ghcr.io/g2rain/g2rain-model-nexus:1.0.0` 和 `sha-<完整提交 SHA>` | GitHub Release、`latest`、包公开、服务部署。 |
| Maven 上传 | 镜像成功后的 reusable 工作流，或手动运行 `Publish API to GitHub Maven Packages` | 根父 POM 与 API JAR、sources JAR 上传 GitHub Maven Packages | 消费仓库已获得包读取权限或解析成功。 |
| 匿名拉取 | 包公开后 `docker pull ghcr.io/g2rain/g2rain-model-nexus:1.0.0` | 包的公共可见性与拉取路径 | 服务配置正确、健康检查成功或生产部署。 |
| 在线演示 | 平台演示入口 | 演示入口的可访问性 | 本组件版本已发布或目标环境已经部署。 |
| 实际部署验证 | 目标环境的发布流程 | 环境依赖、路由、权限和健康检查 | 仅由本地或 CI 构建自动证明。 |

本项目采用 Jib，不使用 Dockerfile 或 `.dockerignore`。Jib 配置位于 `g2rain-model-nexus-startup/pom.xml`：基础镜像为 `eclipse-temurin:25.0.4.1_1-jre`，入口为 `com.g2rain.model.nexus.Application`，声明端口为 `8080`（运行时可由 `SERVER_PORT` 覆盖）。本地构建和 CI 推送复用该配置。OCI 元数据为仓库 source、Apache-2.0 license、title 和 Maven project version。

## GHCR 发布

默认镜像地址是 `ghcr.io/g2rain/g2rain-model-nexus`。发布工作流仅监听 Git tag 推送；它会严格拒绝不是 `v<major>.<minor>.<patch>` 的 tag，并校验 tag 指向的提交已经在 `origin/main` 历史中、`docs/releases/v<版本>.md` 在该提交中且非空、以及 tag 去掉 `v` 后与根 `pom.xml` 的 Maven 项目版本一致。普通分支、Pull Request 和手动触发均不发布。

发布顺序：

1. 将版本、`docs/releases/v<版本>.md`、工作流和部署文档合入 `main`，并在合并后验证。
2. 在该已验证的 `main` 提交创建 `v<版本>` tag，推送 tag。
3. CI 在根目录执行 `mvn --batch-mode --no-transfer-progress clean install`，用 `GITHUB_TOKEN` 登录 GHCR，然后以 `g2rain-model-nexus-startup` 为工作目录执行 Jib，同时推送 `<版本>` 和 `sha-<完整 Git 提交 SHA>`。
4. 仅两个标签都成功推送后，Actions Summary 才会显示镜像地址和两个标签；随后调用同一提交中的 Maven API 发布工作流。Maven 发布失败会使该 job 失败，已经推送的镜像不会自动撤回；两种包发布不具有事务性。

工作流权限仅为 `contents: read` 和 `packages: write`，不使用 PAT Secret；它不创建 GitHub Release、不部署服务，也不更新 `latest`。

首次成功推送后，维护者须在 GitHub Packages 的 **Package settings → Change visibility → Public** 将包设为公开；若组织管理员禁止更改，请联系管理员。已有 GHCR package 可能还需要在 **Package settings → Actions access** 中授予 `g2rain/g2rain-model-nexus` 写权限。若收到 `denied`，先检查该访问设置和组织 Packages 策略，而不是仅以 `packages: write` 推断权限充分。

## Maven API 发布

`.github/workflows/publish-maven.yml` 可由镜像 job 成功后自动调用，也可在 GitHub Actions 中以 **Publish API to GitHub Maven Packages → Run workflow** 手动运行。所选提交必须已进入 `main` 历史；并发组串行处理手动和自动调用。它以 JDK 25 和 Maven 执行 `-pl g2rain-model-nexus-api -am clean deploy`，Reactor 仅包含根父 POM 与 API 模块，不包含 Biz 或 Startup。现有 flatten 插件将 CI friendly `${revision}` 解析为发布版 POM 的具体版本，API 的 source 插件附带 sources JAR。

发布坐标为 `com.g2rain:g2rain-model-nexus-api:1.0.0`，父 POM 为 `com.g2rain:g2rain-model-nexus:1.0.0`；仓库地址为 `https://maven.pkg.github.com/g2rain/g2rain-model-nexus`。工作流通过 setup-java 的 `server-id: github` 和安全注入的 `GITHUB_TOKEN` 认证。重复上传同一稳定版本可能冲突；不要删除或覆盖既有版本，应按仓库版本策略升级后发布。

消费方需在 Maven settings 或 POM 配置该 repository，并以具备包读取权限的凭据认证。跨仓库使用 `GITHUB_TOKEN` 时，须在包的 **Package settings → Actions access** 授予消费仓库读取权限；个人读取可用具备 `read:packages` 的 PAT classic，经受保护的 Secret 注入 Maven settings，绝不可提交 Token。GitHub Maven Packages 的认证要求不同于公开 GHCR 包的匿名拉取。Maven 上传成功不等于消费方已成功解析；须分别验证。

## 部署前检查

- 镜像版本、`sha-<完整提交 SHA>`、Git tag 与根 `pom.xml` 的 `<revision>` 必须相互一致；不使用 `latest`。
- Nacos namespace、配置分组、服务发现、MySQL、Redis 与模型供应商服务均须按环境隔离。
- Gateway 路由、服务身份、授权和 `GET /actuator/health` 健康检查须在目标环境验证。
- 模型目录、接入资料、供应商凭据与协议兼容性须在目标环境验证。
- 发布策略应包含失败回滚；模型调用可能产生外部成本或副作用，重试策略须评估供应商语义。

Jib 最终镜像包含启动模块的应用类、依赖和打包 resources；`g2rain-model-nexus-biz` 的 MyBatis mapper XML 作为 classpath 资源包含在依赖 JAR 中。已跟踪的 `g2rain-model-nexus-startup/src/main/resources/application-dev.yml` 中有本地 MySQL 默认凭据，`application-nacos.yml` 中有 Nacos 默认凭据；这些配置资源会进入应用 JAR，因而也会进入镜像。请在发布前评估并轮换这些值。生产密钥、Token、私有服务地址和凭据必须从平台 Secret、环境变量或受控 Nacos 配置注入，不得使用镜像内默认值。
