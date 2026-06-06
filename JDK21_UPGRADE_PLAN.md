# CAT JDK 21 升级计划

## 目标

将当前 CAT 工程升级到 JDK 21 构建和运行，替换高风险历史依赖，并逐步从“外置 Tomcat + WAR”部署方式迁移到“JDK 21 + 可执行 JAR + 内嵌容器”部署方式。

这次升级不只改版本号。项目仍依赖 `javax.servlet`、`web.xml`、Unidal MVC、Plexus 组件、JSP、历史 Log4j 1.x API 和 MySQL/Netty/Hadoop 等基础库。升级需要分阶段推进，每个阶段都要保持可编译、可测试、可打包、可启动。

## 当前推荐路线

推荐采用“两步容器迁移”：

1. 先把工程构建、运行时和主要基础依赖升级到 JDK 21 可用状态。
2. 新增 `cat-boot` 过渡模块，用 Spring Boot 4 稳定版作为启动外壳，但 Web 容器暂时锁定在 Tomcat 9 线，继续兼容现有 `javax.servlet` / JSP / Unidal MVC。
3. Docker 从外置 Tomcat 镜像切换为 JDK 21 runtime 镜像，通过 `java -jar /app/cat-boot.jar` 启动。
4. 后续单独评估 Jakarta 迁移，确认 Unidal MVC、JSP、Servlet Filter 和插件体系可行后，再考虑真正切换到 Spring Boot 4 原生 Jakarta Web 栈。

不建议第一步直接把现有 Web 层 Jakarta 化。Spring Boot 4 的 Web 生态基于 Jakarta Servlet，而当前项目大量代码和依赖仍是 `javax.servlet`，直接切换会把 JDK、容器、MVC、JSP、Filter、依赖树问题混在一起，风险过高。

## 已完成的升级项

### 1. JDK 21 构建基线

- 根 POM 已改为 `<maven.compiler.release>21</maven.compiler.release>`。
- Maven Enforcer 已要求 Java `[21,22)` 和 Maven `[3.9.0,)`。
- Maven 插件已升级到 JDK 21 兼容版本，包括 compiler、surefire、war、shade、source、javadoc、enforcer。
- GitHub Actions 已从 JDK 8 切换到 JDK 21。
- Docker 构建镜像已切换到 `maven:3.9.12-eclipse-temurin-21`。

### 2. 测试兼容性修复

- 修复 `DefaultMessageTree.copyForTest()` 对消息长度头的处理，并释放 `ByteBuf`。
- `PlainTextMessageCodec` 解码后补充完成态设置。
- 多个 consumer analyzer 测试补充 `setCompleted()`。
- `TaskHelperTest` 移除 PowerMock，用普通 JUnit/Mockito 风格验证。
- `StateAnalyzer` 增加测试所需的 `setMIp(String ip)`。

### 3. 日志组件迁移到 Logback

- Logback 已升级到 `1.5.34`。
- SLF4J 使用 `2.0.17` 稳定线。
- 服务端模块引入 `logback-classic` 和 `log4j-over-slf4j`。
- 排除 Hadoop 传递进来的旧 `slf4j-log4j12` 和 `log4j`。
- 新增 `cat-home/src/main/resources/logback.xml`。
- 测试模块新增 `logback-test.xml` 或测试用 `log4j.properties`，避免测试日志绑定冲突。
- 保留 `log4j:log4j` 的 optional/provided 兼容依赖，用于 CAT 客户端对外暴露的 Log4j 1.x Appender API；它不应作为服务端运行时日志实现。

### 4. JSON 组件迁移到 fastjson2

- `com.alibaba:fastjson` 已替换为 `com.alibaba.fastjson2:fastjson2`。
- fastjson2 已升级到 `2.0.62`。
- 当前源码未发现直接使用 `com.alibaba.fastjson.*` import，本阶段主要是依赖坐标迁移。

### 5. 低风险依赖升级

- JUnit 升级到 `4.13.2`。
- Netty 升级到 `4.1.128.Final`，不采用 Netty 5 alpha。
- Gson 升级到 `2.13.2`。
- HttpClient/HttpMime 升级到 `4.5.14`。
- Commons Codec 升级到 `1.19.0`。
- Snappy 升级到 `1.1.10.8`。
- Freemarker 升级到 `2.3.34`。
- c3p0 升级到 `0.11.2`。
- Plexus Utils 升级到 `4.0.2`。
- java-saml 升级到 `2.9.0`。
- MySQL 驱动坐标从 `mysql:mysql-connector-java` 切换为 `com.mysql:mysql-connector-j:9.7.0`。
- MySQL 驱动类配置更新为 `com.mysql.cj.jdbc.Driver`。

### 6. Spring Boot 过渡启动模块

- 新增 `cat-boot` 模块。
- `cat-boot` 引入 Spring Boot `4.0.2` 稳定版作为启动外壳。
- `cat-boot` 显式使用 `tomcat-embed-core` / `tomcat-embed-jasper` `9.0.112`，继续兼容现有 `javax.servlet` Web 层。
- `cat-boot` 打包时复制 `cat-home.war` 到 classpath，并通过 shade 生成可执行 JAR。
- `CatBootApplication` 启动 Spring Boot 后，由 `EmbeddedCatServer` 解出 `cat-home.war` 并部署到 `/cat`。
- shade 配置已排除 `META-INF/*.SF`、`META-INF/*.DSA`、`META-INF/*.RSA`，避免 JDK 21 下 fat jar 签名校验失败。

### 7. Docker 运行方式切换

- Docker runtime 镜像已从 Tomcat/JRE 8 切换为 `eclipse-temurin:21-jre`。
- Docker 运行入口已改为 `java -Dcat.home=/data/appdatas/cat -Dserver.port=8080 -jar /app/cat-boot.jar`。
- `docker-compose.yml` 已改为基于当前 Dockerfile 构建 `cat:4.0-RC1-jdk21`，不再默认拉取旧的 `meituaninc/cat:3.0.1` 镜像。

## 当前验证结果

已通过的验证：

- `mvn -pl cat-consumer -am test`
- `mvn -pl cat-alarm -am test`
- `mvn -pl cat-boot -am package -DskipTests`
- `mvn package -DskipTests`
- `mvn test`
- `java -Dserver.port=0 -jar cat-boot/target/cat-boot-4.0-RC1.jar` 短启动验证
- `java -Dcat.home=.tmp-cat-home -Dserver.port=18080 -jar cat-boot/target/cat-boot-4.0-RC1.jar` 短启动和 HTTP 探测

短启动中已确认：

- Spring Boot `4.0.2` 启动。
- Embedded Tomcat `9.0.112` 启动。
- CAT Web 应用部署到 `/cat`。
- Netty receiver 启动日志出现。
- 本地未配置 `datasources.xml` / `server.xml` 时会出现数据源缺失日志，这是运行环境配置问题，不是构建失败。
- 使用临时 `cat.home` 访问 `/cat/r` 可到达 Web 层，但返回 500；堆栈指向缺少数据源和 top service 注册，不是 Spring Boot/Tomcat 启动失败。
- 当前机器未安装或未暴露 `docker` 命令，Docker build/compose 端到端验证尚未执行。

## 待完成验证

### 1. 全量打包复验

在最终提交前继续执行：

```bash
mvn package -DskipTests
```

通过后确认：

- `cat-home/target/cat-home-4.0-RC1.war` 存在。
- `cat-boot/target/cat-boot-4.0-RC1.jar` 存在。

### 2. Docker 构建验证

需要在可用 Docker 环境中执行：

```bash
docker build -f docker/Dockerfile .
```

验收标准：

- Maven 构建阶段成功。
- runtime 镜像只依赖 JDK 21 runtime，不依赖外置 Tomcat。
- 镜像内存在 `/app/cat-boot.jar`。

### 3. Docker Compose 端到端验证

需要执行：

```bash
docker compose -f docker/docker-compose.yml up --build
```

验收标准：

- MySQL 容器启动并初始化 `cat` schema。
- CAT 容器启动成功。
- `8080` Web 端口可访问。
- `2280` TCP 接收端口可监听。
- `/cat/r`、`/cat/s` 等路由可访问。
- JSP 页面和静态资源可正常加载。
- Logback 日志正常输出。
- JSON 输出结构保持兼容。

### 4. 带真实配置的运行冒烟

需要提供或挂载真实配置：

- `client.xml`
- `server.xml`
- `datasources.xml`

验收标准：

- MySQL 数据源初始化成功。
- 服务端能写入和读取 CAT 配置。
- 客户端上报链路可打通。
- 报表页面可访问。
- 告警配置和通知链路无启动异常。

## 仍需单独评估的升级项

### 1. Jakarta / Spring Boot 原生 Web 栈迁移

当前 `cat-boot` 是过渡方案，不等于已经完成 Spring Boot 4 原生 Web 迁移。后续需要单独评估：

- `javax.servlet.*` 到 `jakarta.servlet.*` 的代码迁移。
- `web.xml` namespace 和 Servlet/JSP/JSTL 依赖迁移。
- Unidal MVC 是否支持 Jakarta。
- Plexus 组件初始化顺序是否受影响。
- JSP 在新容器中的渲染兼容性。
- Filter dispatch、权限过滤、Domain 过滤是否保持兼容。

### 2. Unidal 升级

Maven 版本检查显示 Unidal framework 有 4.x 线，但它影响 MVC、Plexus、DAL、代码生成和运行时组件，不应和当前 JDK/Spring Boot 过渡升级混在一起。建议后续单独开分支验证。

### 3. Hadoop 升级

Hadoop `2.4.1` 到 3.x 是高风险升级，会影响 HDFS/logview 相关行为和传递依赖。建议单独处理，并补充 HDFS 场景验证。

### 4. 历史兼容 API

CAT 客户端仍对外提供 Log4j 1.x Appender 等历史集成 API。服务端运行时可以迁移到 Logback，但客户端 API 是否彻底移除需要单独评估兼容性和用户影响。

## 提交拆分建议

建议按以下顺序拆分提交：

1. JDK 21 构建基线、Maven 插件、CI 配置。
2. JDK 21 下的测试修复。
3. Logback 迁移。
4. fastjson2 迁移。
5. 低风险依赖升级。
6. 新增 `cat-boot` 过渡启动模块。
7. Docker 从外置 Tomcat 切换为 `java -jar`。
8. 升级计划文档和验证记录。

## 当前结论

当前推荐方案已经进入可继续验证状态：JDK 21 构建、Logback、fastjson2、低风险依赖和 `cat-boot` 可执行 JAR 已完成初步升级。接下来重点不是继续扩大版本升级范围，而是完成 Docker/MySQL/JSP/路由/上报链路的端到端验证，并把 Jakarta 迁移作为后续独立阶段处理。
