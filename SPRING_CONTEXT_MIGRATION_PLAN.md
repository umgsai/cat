# Spring 上下文迁移详细执行计划

## 1. 目标

将 CAT 当前由 Plexus/Unidal Lookup 管理的应用上下文，逐步迁移到 Spring 管理。最终状态是：

1. 应用启动入口由 Spring Boot 管理。
2. 基础设施 Bean、数据源、MyBatis、事务由 Spring 管理。
3. DAO、Repository、Service、Manager、后台任务、Web Controller 由 Spring 创建和装配。
4. 新代码不再依赖 Plexus、Unidal Lookup、Unidal DAL 生成运行时。
5. 最终移除 `plexus-maven-plugin`、`META-INF/plexus/components.xml` 生成链路、旧 Lookup 容器和旧 DAL 依赖。

本计划采用分阶段迁移，不做一次性大替换。原因是当前 Plexus/Unidal 不只是 IoC 容器，还承担了组件生命周期、MVC 请求链路、DAL 代码生成、后台任务启动、日志注入等职责。一次性替换会把数据访问、页面渲染、JSP、Filter、定时任务和组件初始化问题混在一起，风险过高。

推荐路线是：先让 Spring 接管边界清晰的基础设施和 DAO，再迁移 Service/Manager，随后迁移后台任务，最后处理 Web MVC 和旧容器清理。

## 2. 当前状态

已经完成的前置工作：

1. 项目已升级到 JDK 21。
2. 已新增 `cat-boot`，可以通过 Spring Boot 外壳启动内嵌 Tomcat。
3. 日志组件已迁移到 Logback。
4. JSON 组件已迁移到 fastjson2。
5. 已引入 Lombok。
6. `CatApplication.sql` 中的表已生成 MyBatis `DO`、`Mapper`、XML。
7. 主要业务 DAO 已逐步切换到新的 MyBatis Repository 兼容层。
8. 当前项目可以正常启动运行。

当前仍然依赖旧框架的关键点：

1. `cat-core`、`cat-home`、`cat-alarm`、`cat-consumer` 中仍大量使用 `org.unidal.lookup.annotation.Named` 和 `org.unidal.lookup.annotation.Inject`。
2. 当前扫描结果显示，主代码中旧 Lookup 注解相关命中约 1145 处。
3. `ContainerLoader`、`getDefaultContainer()`、`lookup(...)` 运行时查找命中约 33 处。
4. `DataSourceManager` 命中约 67 处。
5. `Initializable`、`LogEnabled`、`ContainerHolder` 生命周期相关命中约 224 处。
6. 新 MyBatis Repository 当前约 50 个，但多数仍继承 `MyBatisRepositorySupport`。
7. `MyBatisRepositorySupport` 当前仍通过 Unidal `DataSourceManager` 创建 MyBatis `SqlSessionFactory`。
8. Web 请求仍主要经过 Unidal Web MVC、旧 Handler、Payload、Model、JSP Viewer 链路。

这说明项目已经具备 Spring 化的基础，但旧容器仍然是主上下文。下一步应优先切断 DAO 基础设施对 Unidal 的依赖。

## 3. 总体原则

1. 每个阶段完成后都必须保持可编译、可打包、可启动。
2. 每个阶段只迁移一个明确边界，避免 DAO、Service、Web、任务调度同时大改。
3. 新增代码使用 Spring 标准方式：`@Configuration`、`@Bean`、`@Component`、`@Service`、`@Repository`、构造器注入、`@Transactional`。
4. 新增 DAO/Repository 不再继承旧 DAO，也不依赖 Unidal DAL。
5. 过渡期允许保留旧业务方法签名，例如 `DalException`、`DalNotFoundException`，用于降低调用方改动成本。
6. `DalNotFoundException` 语义必须保持兼容。查询不到数据时不能被包装成普通 `DalException`，否则旧业务上层会误判为系统错误。
7. Spring 和 Plexus 共存阶段必须明确组件归属。同一个组件不能同时由两个容器创建。
8. 每迁移一类组件，就删除或停用对应的 Plexus role 注册，避免双容器重复初始化。
9. 任何阶段发现运行时风险，都应回滚当前阶段，而不是跨阶段修补。

## 4. 目标架构

迁移完成后的目标链路：

```text
Spring Boot
  -> Spring ApplicationContext
    -> DataSource / TransactionManager
    -> MyBatis SqlSessionFactory / Mapper
    -> Repository
    -> Service / Manager
    -> Scheduler / Background Task
    -> Spring MVC Controller / Filter
```

旧链路逐步退场：

```text
Plexus components.xml
  -> Unidal Lookup
    -> Unidal DAL DataSourceManager
    -> Generated Dao
    -> Unidal MVC Handler
```

## 5. 阶段 0：建立基线和保护网

### 目标

在正式迁移 Spring 上下文前，固定当前可运行状态，确保后续每一步都有可比较的基线。

### 执行步骤

1. 记录当前 Git 状态：

```bash
git status --short
```

2. 执行基础编译：

```bash
mvn -pl cat-core -am compile -DskipTests
mvn -pl cat-alarm,cat-home -am compile -DskipTests
mvn -pl cat-boot -am package -DskipTests "-Dmaven.javadoc.skip=true"
```

3. 启动应用：

```bash
java -jar cat-boot\target\cat-boot-4.0-RC1.jar
```

4. 验证默认启动参数是否生效：

```text
cat.home = C:\Users\Shang\.cat
cat.log.path = C:\Users\Shang\.cat\logs
```

5. 访问关键页面：

```text
http://localhost:8080/cat
http://localhost:8080/cat/r
```

6. 记录启动日志中的关键错误和警告，作为后续对比基线。

### 验收标准

1. 项目可以编译。
2. `cat-boot` 可以打包。
3. 应用可以启动。
4. 页面可以访问。
5. 日志可以输出到控制台和文件。

### 回滚点

此阶段不改代码，只建立基线。

## 6. 阶段 1：建立 Spring 基础设施边界

### 目标

让 Spring 先管理基础设施 Bean，为后续替换 Plexus 提供入口。此阶段不替换 Unidal MVC，不改变页面请求链路。

### 涉及模块

1. `cat-boot`
2. `cat-core`

### 执行步骤

1. 在 `cat-boot` 中建立 Spring 配置包：

```text
com.dianping.cat.boot.config
com.dianping.cat.boot.mybatis
com.dianping.cat.boot.bridge
```

2. 明确 Spring 扫描范围：

```java
@SpringBootApplication(scanBasePackages = {
      "com.dianping.cat.boot"
})
```

暂时不要全量扫描 `com.dianping.cat`，否则大量旧 `@Named` 组件可能被 Spring 误创建。

3. 新增 Spring 上下文桥接类：

```text
SpringContextHolder
```

该类只用于过渡期。新增业务代码禁止通过静态 holder 获取 Bean。

4. 新增启动健康检查 Bean：

```text
CatSpringStartupVerifier
```

检查内容：

1. Spring `ApplicationContext` 已启动。
2. 必要配置已加载。
3. `cat.home` 和 `cat.log.path` 已初始化。

5. 保持 `EmbeddedCatServer` 仍按现有方式部署 `cat-home.war` 到 `/cat`。

### 验证方式

```bash
mvn -pl cat-boot -am package -DskipTests "-Dmaven.javadoc.skip=true"
java -jar cat-boot\target\cat-boot-4.0-RC1.jar
```

### 验收标准

1. Spring Boot 正常启动。
2. 内嵌 Tomcat 正常启动。
3. `/cat` 应用正常部署。
4. 没有重复 Bean、循环依赖、全量扫描导致的初始化异常。

### 回滚点

删除新增 Spring 配置和桥接类即可，不影响现有 Plexus 链路。

## 7. 阶段 2：让 MyBatis 和数据源交给 Spring 管理

### 目标

切断新 MyBatis Repository 对 Unidal `DataSourceManager` 的依赖，让 `DataSource`、`SqlSessionFactory`、`SqlSessionTemplate`、事务全部由 Spring 管理。

这是最推荐优先执行的阶段，边界清晰、收益最大。

### 涉及模块

1. `cat-boot`
2. `cat-core`
3. `cat-home`
4. `cat-alarm`

### 执行步骤

1. 新增 Spring 数据源配置：

```text
com.dianping.cat.boot.mybatis.CatDataSourceConfiguration
```

短期策略：

1. 优先复用当前 `datasources.xml` 或等价连接参数，降低配置变化。
2. 用 Spring Bean 包装出标准 `javax.sql.DataSource`。
3. 保持数据源名称 `cat` 的兼容语义。

中期策略：

1. 将数据库配置迁移到 `application.yml` 或外部配置文件。
2. 使用 Spring Boot 标准 `spring.datasource.*`。
3. 使用 HikariCP 或 Spring Boot 默认连接池。

2. 新增 MyBatis 配置：

```text
com.dianping.cat.boot.mybatis.CatMyBatisConfiguration
```

配置内容：

1. `SqlSessionFactoryBean`
2. `SqlSessionTemplate`
3. `DataSourceTransactionManager`
4. `@MapperScan`
5. Mapper XML 路径加载

Mapper 扫描范围建议先限定在新包：

```text
com.dianping.cat.core.mybatis.mapper
com.dianping.cat.home.mybatis.mapper
com.dianping.cat.alarm.mybatis.mapper
```

3. 改造 `MyBatisRepositorySupport`。

当前问题：

1. 持有 Unidal `DataSourceManager`。
2. 手工构造 `SqlSessionFactory`。
3. Repository 自己管理 `SqlSession`。

目标状态：

1. 不再引用 `DataSourceManager`。
2. 不再手工构造 `SqlSessionFactory`。
3. Repository 通过构造器注入 Mapper。
4. 写操作交给 Spring 事务。

4. 先选择 3 个低风险 Repository 做试点：

```text
ConfigRepository
DailyReportRepository
HostinfoRepository
```

试点原因：

1. `config` 是系统启动时高频读取表，能尽早暴露问题。
2. `dailyreport` 已经迁移过，适合验证报表类查询。
3. `hostinfo` 当前数据可能为空，适合验证空数据语义。

5. 保持兼容方法签名。

短期保留：

```text
DalException
DalNotFoundException
Readset
Updateset
```

但方法内部不再依赖 Unidal DAL 查询引擎。

6. 写操作增加事务边界：

```java
@Transactional
```

7. 删除已迁移 Repository 的 Plexus 注册。

检查位置：

```text
cat-core/src/main/java/com/dianping/cat/build/*DatabaseConfigurator.java
cat-home/src/main/java/com/dianping/cat/build/*DatabaseConfigurator.java
cat-alarm/src/main/java/com/dianping/cat/build/*DatabaseConfigurator.java
```

### 验证方式

编译验证：

```bash
mvn -pl cat-core -am compile -DskipTests
mvn -pl cat-alarm,cat-home -am compile -DskipTests
mvn -pl cat-boot -am package -DskipTests "-Dmaven.javadoc.skip=true"
```

运行验证：

1. 启动应用。
2. 访问配置页。
3. 访问报表页。
4. 检查启动阶段 `server-config` 查询。
5. 检查 `hostinfo` 空表时是否符合旧行为。

重点日志：

1. 不能出现 Mapper 未注册。
2. 不能出现事务管理器缺失。
3. 不能出现 `DataSourceManager` 初始化失败。
4. 查询不到数据时不能误报 ERROR。

### 验收标准

1. 试点 Repository 不再依赖 `DataSourceManager`。
2. 试点 Repository 由 Spring 管理。
3. Mapper 由 Spring 创建。
4. 查询不到数据仍按旧行为抛 `DalNotFoundException`。
5. 页面访问和启动配置加载正常。

### 回滚点

保留旧 `MyBatisRepositorySupport` 实现作为回滚参考。若 Spring MyBatis 配置异常，只回滚试点 Repository 和 Spring MyBatis 配置，不影响其他旧 DAO。

## 8. 阶段 3：批量迁移全部 MyBatis Repository

### 目标

将当前约 50 个 MyBatis Repository 全部交给 Spring 管理，移除 Repository 层对 Unidal `DataSourceManager` 和 Plexus 注册的依赖。

### 执行分组

建议按模块分组迁移。

第一组：`cat-core` 基础表。

```text
config
dailyreport
hourlyreport
weeklyreport
monthreport
hostinfo
project
task
business_config
```

第二组：`cat-home` 页面和配置表。

```text
alert_summary
alteration
baseline
config_modification
metric_graph
metric_screen
overload
topology_graph
```

第三组：`cat-alarm` 告警表。

```text
alert
server_alarm_rule
user_define_rule
```

第四组：报表内容表。

```text
daily_report_content
hourly_report_content
monthly_report_content
weekly_report_content
```

### 执行步骤

1. 每次迁移一个模块或一组表，不跨模块混改。
2. Repository 增加 Spring stereotype：

```java
@Repository
```

3. 使用构造器注入 Mapper。
4. 删除 `extends MyBatisRepositorySupport`。
5. 删除 `openSession()` 手动 session 管理。
6. 删除对应 Plexus role 注册。
7. 保持外部调用方法签名。
8. 保持异常语义兼容。

### 验证方式

每迁移一组执行：

```bash
mvn -pl cat-core -am compile -DskipTests
mvn -pl cat-home -am compile -DskipTests
mvn -pl cat-alarm -am compile -DskipTests
mvn -pl cat-boot -am package -DskipTests "-Dmaven.javadoc.skip=true"
```

检查旧依赖残留：

```bash
rg -n "extends MyBatisRepositorySupport|DataSourceManager" cat-core/src/main/java cat-home/src/main/java cat-alarm/src/main/java
rg -n "C\\(.*Repository\\.class\\).*DataSourceManager" cat-core/src/main/java cat-home/src/main/java cat-alarm/src/main/java
```

### 验收标准

1. 所有新 MyBatis Repository 不再继承 `MyBatisRepositorySupport`。
2. Repository 层不再引用 `DataSourceManager`。
3. Repository 层不再需要 Plexus 注册。
4. 所有 Mapper 能由 Spring 注入。
5. 常用页面和后台任务读写正常。

### 回滚点

按分组回滚。不要在一个提交中混合迁移全部 Repository。

## 9. 阶段 4：迁移配置类 Manager 和基础 Service

### 目标

将配置管理类和基础业务服务从 Plexus 组件迁移为 Spring Bean。

### 优先迁移对象

第一批配置管理类：

```text
ServerConfigManager
AlertConfigManager
SenderConfigManager
BusinessConfigManager
AtomicMessageConfigManager
TpValueStatisticConfigManager
```

第二批基础服务：

```text
HostinfoService
ProjectService
TaskManager
ServerStatisticManager
```

### 执行步骤

1. 将 `@Named` 替换为 Spring 注解：

```java
@Component
@Service
```

2. 将 `org.unidal.lookup.annotation.Inject` 替换为构造器注入。
3. 将 `Initializable.initialize()` 替换为：

```java
@PostConstruct
```

或在需要明确启动顺序时使用：

```java
SmartLifecycle
ApplicationRunner
```

4. 将 `LogEnabled` 替换为 SLF4J：

```java
private static final Logger LOGGER = LoggerFactory.getLogger(CurrentClass.class);
```

5. 删除对应 Plexus role 注册。
6. 对仍由 Plexus 创建、但需要访问 Spring Bean 的旧组件，短期使用桥接适配器。
7. 避免新代码继续调用 `ContainerLoader.getDefaultContainer()`。

### 验证方式

```bash
mvn -pl cat-core -am compile -DskipTests
mvn -pl cat-alarm,cat-home -am compile -DskipTests
mvn -pl cat-boot -am package -DskipTests "-Dmaven.javadoc.skip=true"
```

运行验证：

1. 启动应用。
2. 检查 `server-config` 加载。
3. 修改配置后确认保存和读取正常。
4. 检查配置刷新任务是否正常。
5. 检查日志中没有重复初始化。

### 验收标准

1. 已迁移 Manager/Service 不再使用 Unidal `@Named`。
2. 已迁移 Manager/Service 不再使用 Unidal `@Inject`。
3. 已迁移 Manager/Service 不再依赖 `Initializable` 和 `LogEnabled`。
4. 对应 Plexus role 已删除。
5. 启动和配置读取行为保持兼容。

### 回滚点

按类回滚。配置管理类不要一次性全量迁移。

## 10. 阶段 5：迁移报表 Service 和业务 Manager

### 目标

将报表查询、报表聚合、页面服务类从 Plexus 迁移到 Spring。

### 迁移对象

优先迁移服务类，而不是页面 Handler：

```text
LocalTransactionService
HistoricalTransactionService
TransactionReportService
LocalEventService
HistoricalEventService
EventReportService
LocalProblemService
HistoricalProblemService
ProblemReportService
LocalHeartbeatService
HistoricalHeartbeatService
HeartbeatReportService
```

随后迁移：

```text
DefaultReportManager
AbstractReportService 子类
BusinessReportService
DependencyReportService
StateReportService
StorageReportService
MatrixReportService
TopReportService
```

### 执行步骤

1. 每次选择一个报表域迁移，例如 transaction 或 event。
2. 将该报表域 Service 改为 Spring Bean。
3. 保持 Handler 暂时仍由旧 MVC 创建。
4. 通过桥接方式让旧 Handler 获取 Spring Service。
5. 验证页面结果后，再迁移下一个报表域。

### 验证页面

至少覆盖：

```text
/cat/r/t
/cat/r/e
/cat/r/p
/cat/r/h
/cat/r/state
/cat/r/top
```

具体 URL 以当前路由为准。

### 验收标准

1. 已迁移报表 Service 由 Spring 创建。
2. 旧 Handler 仍可正常调用这些 Service。
3. 报表页面正常渲染。
4. 查询历史报表和实时报表均正常。

### 回滚点

按报表域回滚。某个报表域失败，不影响其他报表域。

## 11. 阶段 6：迁移后台任务和调度

### 目标

用 Spring 生命周期和 Spring 调度机制管理后台任务，替代 Plexus 初始化期间启动任务的方式。

### 迁移对象

```text
TimerSyncTask
ReportReloadTask
DefaultTaskConsumer
ProjectUpdateTask
CurrentReportBuilder
各类 ReportBuilder
各类 ReportReloader
CapacityUpdateTask
```

### 执行步骤

1. 新增 Spring 调度配置：

```java
@EnableScheduling
```

2. 配置统一线程池：

```text
ThreadPoolTaskScheduler
```

3. 将手动启动线程替换为：

```java
@Scheduled
SmartLifecycle
ApplicationRunner
```

4. 每个任务增加开关配置，支持临时关闭。
5. 任务启动日志必须包含任务名称和调度周期。
6. 应用关闭时任务必须优雅停止。
7. 防止同一任务被 Plexus 和 Spring 重复启动。

### 验证方式

1. 启动应用，观察任务启动日志。
2. 检查同一任务是否只启动一次。
3. 触发报表 reload。
4. 触发项目更新任务。
5. 关闭应用，确认线程池正常退出。

### 验收标准

1. 已迁移任务由 Spring 生命周期管理。
2. 任务不会重复启动。
3. 应用关闭无挂起线程。
4. 任务异常有统一日志。

### 回滚点

按任务回滚。先迁移低风险任务，再迁移核心报表生成任务。

## 12. 阶段 7：迁移 Web 层到 Spring MVC

### 目标

用 Spring MVC 替换 Unidal Web MVC 请求生命周期。

这是风险最高、改动最大的阶段，应在 DAO、Service、Manager、任务基本迁移完成后再执行。

### 当前旧链路

```text
org.unidal.web.MVC
DefaultRequestLifecycle
Page Handler
Payload
Model
BaseJspViewer
JSP
```

### 迁移策略

优先采用页面级渐进迁移：

1. 第一阶段：Spring Controller 包装旧 Handler。
2. 第二阶段：逐个页面替换为 Spring MVC Controller。
3. 第三阶段：替换 Payload/Model 绑定方式。
4. 第四阶段：替换 JSP Viewer。
5. 第五阶段：删除 Unidal MVC。

### 优先页面

先迁移低风险页面：

```text
home
config
state
logview
```

再迁移核心报表页面：

```text
transaction
event
problem
heartbeat
business
dependency
matrix
top
```

最后迁移告警和复杂配置页面：

```text
alert
router config
overload
storage
statistics
```

### 执行步骤

1. 新增 Spring MVC 配置。
2. 配置 JSP ViewResolver。
3. 保持静态资源路径兼容。
4. 迁移 Filter：

```text
PermissionFilter
DomainFilter
CatFilter
```

5. 增加统一异常处理：

```java
@ControllerAdvice
```

6. 逐个页面迁移 Controller。
7. 保持原 URL 尽量不变。
8. 每迁移一个页面就删除对应旧路由注册。

### 验证页面

至少覆盖：

```text
http://localhost:8080/cat
http://localhost:8080/cat/r
http://localhost:8080/cat/r/t
http://localhost:8080/cat/r/e
http://localhost:8080/cat/r/p
配置页面
告警页面
报表历史查询页面
```

### 验收标准

1. 页面可以访问。
2. JSP 正常渲染。
3. 静态资源正常加载。
4. 权限、domain、跳转逻辑正常。
5. 原有 URL 尽量兼容。
6. 已迁移页面不再经过 Unidal MVC。

### 回滚点

按页面回滚。不要一次性替换全部 MVC。

## 13. 阶段 8：移除 Plexus/Unidal Lookup

### 目标

彻底移除旧容器。

### 前置条件

必须同时满足：

1. DAO 已全部由 Spring 管理。
2. Repository 不再依赖 `DataSourceManager`。
3. Service/Manager 已由 Spring 管理。
4. 后台任务已由 Spring 管理。
5. Web 层已迁移到 Spring MVC，或不再依赖 Unidal MVC 生命周期。
6. 运行时代码不再调用 `ContainerLoader.getDefaultContainer()`。
7. 构建不再需要生成 `components.xml`。

### 执行步骤

1. 删除 `plexus-maven-plugin`。
2. 删除 `META-INF/plexus/components.xml` 生成流程。
3. 删除旧组件注册类。
4. 删除 `org.unidal.lookup` 运行时依赖。
5. 删除 `ContainerHolder`、`ContainerLoader` 使用。
6. 删除旧 DAL 生成流程：

```text
codegen-maven-plugin dal-jdbc
Readset
Updateset
旧 *Dao
```

7. 清理 POM 依赖：

```text
org.unidal.framework:dal-jdbc
org.unidal.framework:foundation-service
org.unidal.framework:web-framework
org.unidal.framework:test-framework
```

注意：只有当没有代码引用时才能删除。

### 验证方式

```bash
mvn clean package -DskipTests
mvn test
```

运行验证：

1. 应用启动。
2. 页面访问。
3. 数据库读写。
4. 客户端上报。
5. 报表生成。
6. 告警链路。
7. 应用关闭。

### 验收标准

1. 依赖树中不再需要 Plexus/Unidal Lookup。
2. 构建不再生成 `components.xml`。
3. 运行时没有 Plexus 容器初始化日志。
4. 核心功能仍可用。

### 回滚点

此阶段风险最高，只能在前面阶段全部完成并稳定后执行。建议单独提交，单独验证。

## 14. 每阶段通用检查清单

每完成一个阶段或一组类迁移，都执行以下检查。

### 编译和打包

```bash
mvn -pl cat-core -am compile -DskipTests
mvn -pl cat-alarm,cat-home -am compile -DskipTests
mvn -pl cat-boot -am package -DskipTests "-Dmaven.javadoc.skip=true"
```

### 旧 DAO import 检查

```bash
rg -n "import com\.dianping\.cat\.(core\.dal|core\.config|home\.dal\.report|alarm)\.[A-Za-z0-9]+Dao;" cat-core/src/main/java cat-home/src/main/java cat-alarm/src/main/java cat-consumer/src/main/java
```

### 旧容器查找检查

```bash
rg -n "ContainerLoader|getDefaultContainer|lookup\(" cat-core/src/main/java cat-home/src/main/java cat-alarm/src/main/java cat-consumer/src/main/java
```

### Plexus 注解和生命周期检查

```bash
rg -n "org\.unidal\.lookup\.annotation|@Named|Initializable|LogEnabled|ContainerHolder" cat-core/src/main/java cat-home/src/main/java cat-alarm/src/main/java cat-consumer/src/main/java
```

### 数据源旧依赖检查

```bash
rg -n "DataSourceManager|MyBatisRepositorySupport" cat-core/src/main/java cat-home/src/main/java cat-alarm/src/main/java cat-consumer/src/main/java
```

### 运行验证

1. 启动应用。
2. 访问首页。
3. 访问报表页。
4. 访问配置页。
5. 修改并保存一项配置。
6. 检查日志文件。
7. 检查控制台 ERROR。
8. 正常停止应用。

## 15. 风险和应对

### 风险 1：双容器重复创建组件

表现：

1. 初始化执行两次。
2. 后台任务重复启动。
3. 配置重复加载。

应对：

1. Spring 扫描范围保持收敛。
2. 每迁移一个组件就删除对应 Plexus role。
3. 后台任务迁移时增加启动日志和唯一性检查。

### 风险 2：异常语义不兼容

表现：

1. 查询不到数据被记录为 ERROR。
2. 旧业务上层逻辑进入异常分支。

应对：

1. `DalNotFoundException` 必须原样透传。
2. Repository 不要统一包装所有异常。
3. 为核心查询补充单元测试。

### 风险 3：事务边界变化

表现：

1. 写操作未提交。
2. 多表更新部分成功。
3. 连接泄漏。

应对：

1. 写操作统一加 `@Transactional`。
2. Repository 不再手动管理 session。
3. 用 Spring `DataSourceTransactionManager` 管理事务。

### 风险 4：启动顺序变化

表现：

1. 配置还未加载，任务已启动。
2. Web 页面访问时 Service 未初始化。

应对：

1. 对关键 Bean 使用明确生命周期。
2. 后台任务使用 `SmartLifecycle` 或 `ApplicationReadyEvent`。
3. 不依赖字段注入的隐式顺序。

### 风险 5：Web 层迁移破坏 URL 和 JSP

表现：

1. 原 URL 404。
2. JSP tag 或静态资源缺失。
3. 权限过滤逻辑失效。

应对：

1. Web 层最后迁移。
2. 按页面迁移。
3. 保留原 URL。
4. 每个页面迁移后单独验收。

## 16. 推荐下一步

建议下一步从阶段 2 开始，先做一个最小可验证闭环：

1. 新增 Spring `DataSource`、`SqlSessionFactory`、`SqlSessionTemplate`、`TransactionManager` 配置。
2. 只迁移 `ConfigRepository`、`DailyReportRepository`、`HostinfoRepository` 三个 Repository。
3. 保持旧方法签名和异常语义。
4. 删除这三个 Repository 对 `DataSourceManager` 的依赖。
5. 启动应用验证 `server-config`、报表查询、空 `hostinfo` 表行为。
6. 通过后再批量迁移其他 Repository。

这个顺序收益最大，因为它先把 DAO 基础设施从 Unidal 中解耦出来，同时不会立即触碰最高风险的 Web MVC 链路。

## 17. 2026-06-09 当前结论与下一步计划

### 当前代码基线

用户已提交上一轮日志补充和 Spring 迁移相关代码。以 2026-06-09 当前工作树为新基线，`git status --short` 为空。

当前项目仍不能直接删除 Plexus / Unidal 依赖。原因是旧框架仍承担三类核心职责：

1. 组件容器和运行时查找：仍有约 27 个生产 Java 文件使用 `ContainerHolder`、`lookup(...)`、`lookupMap(...)`。
2. Unidal Web MVC：`cat-home` 中约 146 个生产 Java 文件仍涉及 `org.unidal.web`、`PageHandler`、`ActionPayload`、`ViewModel`、`BaseJspViewer`、`FieldMeta` 等。
3. Unidal DAL / codegen：约 154 个生产 Java 文件仍涉及 `org.unidal.dal`、`DalException`、`DalNotFoundException` 或生成 DAO / model。

根 `pom.xml` 中仍存在以下关键依赖或插件管理项：

```text
org.unidal.framework:dal-jdbc
org.unidal.framework:foundation-service
org.unidal.framework:web-framework
org.unidal.framework:test-framework
org.unidal.webres:WebResServer
org.unidal.maven.plugins:codegen-maven-plugin
org.unidal.maven.plugins:plexus-maven-plugin
```

源码资源中仍存在 Plexus 组件描述文件：

```text
cat-alarm/src/main/resources/META-INF/plexus/components.xml
cat-consumer/src/main/resources/META-INF/plexus/components.xml
cat-core/src/main/resources/META-INF/plexus/components.xml
cat-hadoop/src/main/resources/META-INF/plexus/components.xml
cat-home/src/main/resources/META-INF/plexus/components.xml
```

这些文件暂时不能提前删除，必须等对应 Spring 替代链路验证通过后再分批移除。

### 已存在的迁移桥接基础

当前已经具备继续迁移的 Spring 桥接基础：

1. `cat-home/src/main/java/com/dianping/cat/home/spring/CatHomeSpringContextListener.java` 已创建 Spring `AnnotationConfigApplicationContext`，并将上下文写入 `ServletContext` 和 `CatSpringContext`。
2. `cat-home/src/main/java/com/dianping/cat/home/spring/CatHomeSpringConfiguration.java` 已手动注册大量 Repository、ConfigManager、Service Bean。
3. `cat-core/src/main/java/com/dianping/cat/spring/CatSpringContext.java` 已提供旧代码获取 Spring Bean 的过渡桥。
4. `cat-boot/src/main/java/com/dianping/cat/boot/CatBootApplication.java` 已作为 Spring Boot 启动入口。

但当前 `cat-home/src/main/webapp/WEB-INF/web.xml` 仍将 `/r/*` 和 `/s/*` 交给 `org.unidal.web.MVC`，`CatServlet` 也仍依赖 `AbstractContainerServlet`、`DefaultModuleContext`、`ModuleInitializer`。因此 Web MVC 和启动模块初始化仍属于高风险区域，暂不作为下一步优先项。

### 迁移顺序调整

截至 2026-06-09，推荐迁移顺序调整为：

1. 先替换组件容器和 `lookupMap(...)` 型扩展点。
2. 再移除 Plexus 生命周期和日志接口，例如 `Initializable`、`LogEnabled`、`org.codehaus.plexus.logging.Logger`。
3. 然后迁移 Unidal Web MVC。
4. 再继续替换 Unidal DAL / codegen。
5. 最后移除 `plexus-maven-plugin`、`codegen-maven-plugin`、`components.xml` 和根 POM 中的 Unidal 依赖。

此顺序的原因是：如果先迁移 Web MVC 或 DAL，会同时牵动 URL 路由、JSP、Filter、Repository、异常语义和启动流程；而 `lookupMap(...)` 型 Manager 边界较清晰，可以先通过 Spring `Map<String, Bean>` / `List<Bean>` 注入建立替代链路，并保留旧 Plexus fallback 作为回滚保护。

### 下一步执行项

下一步建议迁移第一组低风险 `lookupMap(...)` 管理器：

```text
cat-alarm/src/main/java/com/dianping/cat/alarm/spi/sender/SenderManager.java
cat-alarm/src/main/java/com/dianping/cat/alarm/spi/decorator/DecoratorManager.java
cat-alarm/src/main/java/com/dianping/cat/alarm/spi/spliter/SpliterManager.java
```

执行方式：

1. 为 Manager 增加 Spring 注入入口，例如 setter 或构造器注入。
2. 优先使用 Spring 注入的 `Map<String, Sender>`、`Map<String, Decorator>`、`Map<String, Spliter>`。
3. 暂时保留原有 `lookupMap(...)` fallback，确保 Spring Bean 未完全注册时旧链路仍可运行。
4. 在 `CatHomeSpringConfiguration` 中注册 Manager 及其扩展点 Bean。
5. 增加必要的 `info` / `warn` 日志，明确当前使用的是 Spring 注入还是 Plexus fallback。
6. 编译并启动验证告警相关初始化、配置页和常用 `/cat/r/*` 页面。

### 验证命令

```powershell
mvn -pl cat-alarm,cat-home -am compile -DskipTests
mvn -pl cat-boot -am package -DskipTests "-Dmaven.javadoc.skip=true"
java -Dserver.port=18080 -Dcat.tcp.port=12280 -jar cat-boot\target\cat-boot-4.0-RC1.jar
```

建议验证 URL：

```text
http://127.0.0.1:18080/cat
http://127.0.0.1:18080/cat/r
http://127.0.0.1:18080/cat/r/business
http://127.0.0.1:18080/cat/s/config
```

验收标准：

1. 编译和打包通过。
2. 应用能启动，且没有重复初始化同一 Manager。
3. 告警 SPI Manager 能正常加载扩展点。
4. 日志能看出 Spring 注入是否生效。
5. 如果 Spring 注入缺失，应有明确 warn，并能回退到 Plexus `lookupMap(...)`。
6. `/cat/r/business` 和配置页保持可访问。

### 暂缓事项

以下事项暂缓，不进入下一步改动：

1. 暂不修复用户当前环境中没有复现的 `/cat/r/business` NPE。
2. 暂不删除任何 `META-INF/plexus/components.xml`。
3. 暂不移除根 POM 中的 Unidal / Plexus 依赖。
4. 暂不迁移 `org.unidal.web.MVC`。
5. 暂不批量替换所有 DAL / codegen 相关代码。
