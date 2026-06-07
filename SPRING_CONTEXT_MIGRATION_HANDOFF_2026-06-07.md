# CAT Spring 上下文迁移交接文档（2026-06-07）

## 1. 文档目的

这份文档用于下一次任务开始时快速恢复上下文。下一次继续迁移前，优先阅读本文，再结合当前 `git status --short` 和最近编译结果判断从哪一步继续。

当前项目路径：

```text
D:\workspace\cat
```

当前总体目标：

```text
逐步废弃 Plexus / Unidal Lookup / Unidal DAL 运行时依赖，让应用上下文、数据访问、Service、Manager、后台任务和 Web 层逐步迁移到 Spring 体系。
```

当前迁移策略：

1. 不一次性删除 Plexus。
2. 先把 DAO / Repository / 配置 Manager / 基础 Service 接入 Spring。
3. 旧 Unidal MVC Handler 暂时保留，由 Handler 通过桥接方式使用 Spring Bean。
4. 每一步完成后都编译、打包、启动验证。
5. 不确定运行结果时，停下来让用户在 IDEA 或本地环境验证。

## 2. 当前可运行状态

截至本交接文档编写时，用户已经多次确认项目可以正常启动和访问，最近一次用户反馈是：

```text
已验证 ok / 没问题，继续。
```

已知当前运行形态：

1. JDK 已升级到 21。
2. 项目通过 `cat-boot` 以可执行 JAR 启动。
3. `cat-boot` 内嵌 Tomcat，部署 `cat-home.war` 到 `/cat`。
4. 外置 Tomcat 已废弃。
5. Logback 已替换旧日志实现。
6. JSON 依赖已迁移到 fastjson2。
7. Lombok 已引入。
8. MyBatis 已覆盖 `CatApplication.sql` 中的表，并已有大量 Repository 迁移。
9. Spring 上下文已建立，并能被旧 Plexus 对象通过 `CatSpringContext` 桥接访问。

常用启动参数：

```text
-Dcat.home=C:\Users\Shang\.cat
-Dcat.log.path=C:\Users\Shang\.cat\logs
```

常用访问入口：

```text
http://127.0.0.1:8080/cat
http://127.0.0.1:8080/cat/r
http://127.0.0.1:8080/cat/s/config
http://127.0.0.1:8080/cat/s/router?op=json&domain=cat&ip=127.0.0.1
```

临时验证启动建议使用非默认端口，避免和用户 IDEA 启动实例冲突：

```powershell
java -Dserver.port=18080 -Dcat.tcp.port=12280 -jar cat-boot\target\cat-boot-4.0-RC1.jar
```

自己启动的 Java 进程验证完成后必须停止。

## 3. 今天左右已经完成的主要改动

### 3.1 JDK 21 和 Spring Boot 启动外壳

已完成：

1. 根 POM 和模块 POM 已适配 JDK 21。
2. 新增 `cat-boot` 模块。
3. `CatBootApplication` 已作为 Spring Boot 启动入口。
4. `cat-boot` 打包时包含并部署 `cat-home.war`。
5. 默认系统参数已补齐：

```text
cat.home=C:\Users\Shang\.cat
cat.log.path=C:\Users\Shang\.cat\logs
```

相关重点文件：

```text
cat-boot/src/main/java/com/dianping/cat/boot/CatBootApplication.java
cat-boot/src/main/java/com/dianping/cat/boot/config/
```

### 3.2 日志迁移

已完成：

1. 日志实现切换为 Logback。
2. 引入新版 Logback。
3. 增加 `logback.xml`。
4. 参考 `D:\workspace\lawyer-system` 的风格补充了日志文件输出配置。
5. 处理过错误日志没有落入 `error.log` 的问题。

日志目录期望位置：

```text
C:\Users\Shang\.cat\logs
```

注意：

1. 控制台仍可能出现 CAT 客户端连接自身服务端的 WARN。
2. Logback scan 相关 “Missing watchable .xml” 警告可以暂时忽略。
3. 是否进入 `error.log` 取决于 logger、appender 和 level 配置，后续继续改日志时要实际触发 ERROR 验证。

### 3.3 fastjson2 和 Lombok

已完成：

1. JSON 依赖迁移为 fastjson2。
2. 项目中直接 `com.alibaba.fastjson.*` import 已检查并处理。
3. Lombok 已引入依赖和编译配置。

### 3.4 MyBatis DAO / Repository 迁移

已完成：

1. 基于 `CatApplication.sql` 为所有表生成 MyBatis DO / Mapper / XML。
2. `config` 表迁移为独立 MyBatis DAO，不再继承旧 DAO。
3. `dailyreport` 表迁移到 MyBatis。
4. 后续已扩展到所有表。
5. 新 DAO 包和命名已按用户要求调整，例如 `ConfigMapper`、`ConfigDO`。
6. 目标是新 DAO 层不依赖旧框架。

当前重要结构：

```text
cat-core/src/main/java/com/dianping/cat/core/mybatis/
cat-core/src/main/resources/mybatis/mapper/
cat-home/src/main/resources/mybatis/mapper/
cat-alarm/src/main/resources/mybatis/mapper/
```

已知语义：

1. `hostinfo` 表为空是正常场景，不应被当作错误。
2. 查询不到数据时要尽量保持旧语义，例如该抛 `DalNotFoundException` 的地方不能被统一包装成普通 `DalException`。

### 3.5 Spring 上下文基础设施

已完成：

1. 新增 Spring 上下文桥接类：

```text
cat-core/src/main/java/com/dianping/cat/spring/CatSpringContext.java
```

2. 新增 Spring Home 配置：

```text
cat-home/src/main/java/com/dianping/cat/home/spring/CatHomeSpringConfiguration.java
```

3. `CatSpringContext` 支持旧对象通过类型获取 Spring Bean：

```java
CatSpringContext.getBeanIfAvailable(SomeClass.class)
```

4. `CatHomeSpringConfiguration` 已注册：

```text
DataSource
SqlSessionFactory
SqlSessionTemplate
TransactionManager
TransactionTemplate
ConfigRepository
ProjectRepository
HostinfoRepository
各类 Report Repository
各类告警/配置相关 Repository
ProjectService
BusinessConfigManager
ServerConfigManager
SampleConfigManager
ServerFilterConfigManager
ReportReloadConfigManager
AtomicMessageConfigManager
TpValueStatisticConfigManager
AlertSummaryService
UserDefinedRuleManager
BaselineService
RemoteServersManager
DomainValidator
ServerStatisticManager
```

最近一次启动验证看到 Spring Home 侧 Bean 数量约为：

```text
beanCount=76
```

### 3.6 Repository 和 Service 桥接

已完成并验证的桥接点包括：

```text
AbstractReportService
TaskManager
DefaultTaskConsumer
system/page/config/Handler 的 ConfigModificationRepository
AlertSummaryService
UserDefinedRuleManager
BaseRuleConfigManager
RelatedSummaryBuilder
BusinessBaselineReportBuilder
AbstractGraphCreator
```

已注册或桥接的 Repository / Service 包括：

```text
AlertRepository
AlterationRepository
BaselineRepository
TopologyGraphRepository
TaskRepository
AlertSummaryRepository
ConfigModificationRepository
MetricGraphRepository
MetricScreenRepository
ServerAlarmRuleRepository
UserDefineRuleRepository
AlertSummaryService
UserDefinedRuleManager
BaselineService(DefaultBaselineService)
```

这些改动的核心思路：

```text
旧对象仍由 Plexus 创建，但运行前优先从 Spring 上下文刷新已迁移 Bean。
```

这样可以避免一次性让 Spring 接管所有旧组件造成双容器初始化或字段注入为空。

## 4. 最近已经验证过的命令

常用编译验证：

```powershell
mvn -pl cat-home -am compile -DskipTests
```

常用打包验证：

```powershell
mvn -pl cat-boot -am package -DskipTests "-Dmaven.javadoc.skip=true"
```

常用临时启动验证：

```powershell
java -Dserver.port=18080 -Dcat.tcp.port=12280 -jar cat-boot\target\cat-boot-4.0-RC1.jar
```

常用 HTTP 验证：

```text
http://127.0.0.1:18080/cat
http://127.0.0.1:18080/cat/r
http://127.0.0.1:18080/cat/s/config
http://127.0.0.1:18080/cat/s/router?op=json&domain=cat&ip=127.0.0.1
```

可暂时忽略的日志：

```text
Unable to connect to CAT server /127.0.0.1:2280
请求 http://127.0.0.1:8080/cat/s/router?... 失败
Logback Missing watchable .xml...
```

端口注意：

1. 默认 TCP 接收端口是 `2280`。
2. 如果出现 `Address already in use: bind`，优先确认是否已有 CAT 实例或其他进程占用。
3. 临时验证用 `-Dcat.tcp.port=12280` 可以绕开默认端口冲突。

## 5. 当前正在推进但尚未完成的工作

当前最近一轮准备推进的是：

```text
配置页 Processor Spring 化。
```

目标不是马上删除 Plexus 注册，而是让配置页 Processor 在执行前刷新 Spring 已迁移依赖。

涉及文件：

```text
cat-home/src/main/java/com/dianping/cat/system/page/config/Handler.java
cat-home/src/main/java/com/dianping/cat/system/page/config/processor/BaseProcesser.java
cat-home/src/main/java/com/dianping/cat/system/page/config/processor/GlobalConfigProcessor.java
cat-home/src/main/java/com/dianping/cat/system/page/config/processor/AlertConfigProcessor.java
cat-home/src/main/java/com/dianping/cat/system/page/config/processor/DependencyConfigProcessor.java
cat-home/src/main/java/com/dianping/cat/system/page/config/processor/HeartbeatConfigProcessor.java
cat-home/src/main/java/com/dianping/cat/system/page/config/processor/ExceptionConfigProcessor.java
cat-home/src/main/java/com/dianping/cat/system/page/config/processor/TransactionConfigProcessor.java
cat-home/src/main/java/com/dianping/cat/system/page/config/processor/EventConfigProcessor.java
cat-home/src/main/java/com/dianping/cat/system/page/config/processor/StorageConfigProcessor.java
```

已经确认的现状：

1. `Handler` 已有 `refreshSpringBeans()`，当前主要刷新 `ConfigModificationRepository`。
2. `GlobalConfigProcessor` 已有 `refreshSpringBeans()`，当前刷新了：

```text
ProjectService
SampleConfigManager
ServerConfigManager
ServerFilterConfigManager
ReportReloadConfigManager
```

3. `GlobalConfigProcessor` 还可以继续刷新但尚未补齐的依赖：

```text
RouterConfigManager
DomainGroupConfigManager
StorageGroupConfigManager
```

这些 Manager 自身已具备 `refreshSpringBeans()` 能力，后续可考虑注册为 Spring Bean 或继续通过旧对象内部刷新。

4. `AlertConfigProcessor` 依赖：

```text
AlertConfigManager
AlertPolicyManager
ConfigHtmlParser
```

这几个暂时不建议直接让 Spring 新建 Processor 后替换旧 Processor，除非先把所有字段依赖补齐。

5. `DependencyConfigProcessor` 依赖：

```text
GlobalConfigProcessor
TopologyGraphConfigManager
TopoGraphFormatConfigManager
ConfigHtmlParser
```

`TopologyGraphConfigManager` 和 `TopoGraphFormatConfigManager` 已经有内部 Spring 刷新 `ConfigRepository` / `ContentFetcher` 的逻辑。

6. `HeartbeatConfigProcessor` 依赖：

```text
HeartbeatRuleConfigManager
HeartbeatDisplayPolicyManager
ConfigHtmlParser
```

`HeartbeatDisplayPolicyManager` 已经有内部 Spring 刷新逻辑；`HeartbeatRuleConfigManager` 继承 `BaseRuleConfigManager`，已能刷新 `ConfigRepository`、`ContentFetcher`、`UserDefinedRuleManager`。

7. `ExceptionConfigProcessor` 依赖：

```text
GlobalConfigProcessor
ExceptionRuleConfigManager
```

`ExceptionRuleConfigManager` 已有内部 Spring 刷新逻辑。

8. `TransactionConfigProcessor` 和 `EventConfigProcessor` 依赖：

```text
TransactionRuleConfigManager
EventRuleConfigManager
RuleFTLDecorator（来自 BaseProcesser）
```

这两个 RuleConfigManager 继承 `BaseRuleConfigManager`，已能刷新部分 Spring 依赖。`RuleFTLDecorator` 暂时不建议急着 Spring 化，因为涉及模板装饰器和旧 components 配置。

9. `StorageConfigProcessor` 当前基本是空操作，风险较低。

## 6. 配置 Processor 迁移的推荐下一步

下一次建议按下面顺序执行。

### 6.1 不直接切换为 Spring 新建 Processor

不要马上在 `CatHomeSpringConfiguration` 中注册 8 个 Processor 并让 Handler 使用 Spring Bean。

原因：

1. 当前 Processor 仍依赖大量 `@Inject` 字段。
2. 如果 Spring `new Processor()`，这些 Plexus 字段不会自动注入。
3. 直接替换会导致运行时 NPE，尤其是 `ConfigHtmlParser`、`RuleFTLDecorator`、各类 ConfigManager。

推荐策略：

```text
Processor 仍由 Plexus 创建；Processor 执行前主动刷新 Spring 已迁移依赖。
```

### 6.2 先补 `ConfigHtmlParser` Spring Bean

`ConfigHtmlParser` 是无状态工具类，可以低风险注册到 Spring。

建议在：

```text
cat-home/src/main/java/com/dianping/cat/home/spring/CatHomeSpringConfiguration.java
```

增加：

```java
@Bean
public ConfigHtmlParser configHtmlParser() {
    return new ConfigHtmlParser();
}
```

### 6.3 给 Processor 增加 `refreshSpringBeans()`

建议补充：

1. `BaseProcesser.refreshSpringBeans()`：

```text
可先不刷新 RuleFTLDecorator，避免模板相关风险。
```

2. `GlobalConfigProcessor.refreshSpringBeans()` 继续补：

```text
RouterConfigManager
DomainGroupConfigManager
StorageGroupConfigManager
ConfigHtmlParser
```

3. `AlertConfigProcessor.refreshSpringBeans()`：

```text
ConfigHtmlParser
如果 AlertConfigManager / AlertPolicyManager 已注册 Spring，再刷新；否则保留 Plexus 注入。
```

4. `DependencyConfigProcessor.refreshSpringBeans()`：

```text
GlobalConfigProcessor
TopologyGraphConfigManager
TopoGraphFormatConfigManager
ConfigHtmlParser
```

这里要谨慎：如果 `GlobalConfigProcessor` 没有作为 Spring Bean 注册，就不要替换它；保留 Plexus 字段即可。

5. `HeartbeatConfigProcessor.refreshSpringBeans()`：

```text
HeartbeatDisplayPolicyManager
ConfigHtmlParser
HeartbeatRuleConfigManager 如果已注册 Spring 再替换
```

6. `ExceptionConfigProcessor.refreshSpringBeans()`：

```text
ExceptionRuleConfigManager 如果已注册 Spring 再替换
GlobalConfigProcessor 如果已注册 Spring 再替换
```

7. `TransactionConfigProcessor` / `EventConfigProcessor`：

```text
先只在 process() 开头调用 BaseProcesser.refreshSpringBeans()
是否替换 RuleConfigManager 等确认 Spring Bean 注册后再做
```

### 6.4 Handler 只做刷新，不强切 Processor

`Handler.refreshSpringBeans()` 下一步可以扩展成：

```text
刷新 ConfigModificationRepository
必要时调用各 Processor 的 refreshSpringBeans()
```

但不建议直接：

```java
m_globalConfigProcessor = CatSpringContext.getBeanIfAvailable(GlobalConfigProcessor.class);
```

除非该 Processor 的所有依赖都已经由 Spring 明确注入或手动 setter 完成。

## 7. 配置 Processor 迁移后的验证清单

完成上述小步后，至少执行：

```powershell
mvn -pl cat-home -am compile -DskipTests
mvn -pl cat-boot -am package -DskipTests "-Dmaven.javadoc.skip=true"
```

如果编译和打包通过，临时启动：

```powershell
java -Dserver.port=18080 -Dcat.tcp.port=12280 -jar cat-boot\target\cat-boot-4.0-RC1.jar
```

访问配置页：

```text
http://127.0.0.1:18080/cat/s/config
http://127.0.0.1:18080/cat/s/config?op=serverConfigUpdate
http://127.0.0.1:18080/cat/s/config?op=sampleConfigUpdate
http://127.0.0.1:18080/cat/s/config?op=reportReloadConfigUpdate
http://127.0.0.1:18080/cat/s/config?op=alertPolicy
http://127.0.0.1:18080/cat/s/config?op=transactionRule
http://127.0.0.1:18080/cat/s/config?op=eventRule
http://127.0.0.1:18080/cat/s/config?op=heartbeatRuleConfigList
```

重点检查：

1. 无 Processor 字段 NPE。
2. 无 Mapper 未注册。
3. 无 Spring Bean 循环依赖。
4. 无重复初始化导致的任务重复启动。
5. 配置页至少能返回页面或旧系统可接受的业务错误。

验证结束后停止自己启动的 Java 进程。

## 8. 后续总体计划

### 8.1 短期计划：继续扩大 Spring 桥接面

优先级从高到低：

1. 配置 Processor 内部依赖刷新。
2. 配置相关 Manager 注册或桥接到 Spring。
3. 报表 Builder / Reloader / Task 继续桥接 Spring Repository 和 Service。
4. 旧 Handler 逐步只保留请求分发和 JSP 渲染职责，业务依赖尽量来自 Spring。

### 8.2 中期计划：迁移后台任务

目标：

```text
用 Spring 生命周期和调度机制替代 Plexus 初始化阶段启动任务。
```

候选对象：

```text
TimerSyncTask
ReportReloadTask
DefaultTaskConsumer
ProjectUpdateTask
CurrentReportBuilder
各类 ReportBuilder
各类 ReportReloader
CapacityUpdateStatusManager
DailyCapacityUpdater
HourlyCapacityUpdater
WeeklyCapacityUpdater
MonthlyCapacityUpdater
```

建议：

1. 先加 Spring 调度基础设施。
2. 每次只迁移一个任务族。
3. 每个任务迁移后确认没有被 Plexus 和 Spring 重复启动。
4. 任务迁移时一定要增加启动日志和停止逻辑。

### 8.3 长期计划：迁移 Web 层

最后再处理 Web 层，因为风险最大。

当前旧链路：

```text
Unidal Web MVC -> Page Handler -> Payload -> Model -> JspViewer -> JSP
```

目标链路：

```text
Spring MVC Controller -> Service -> ModelAndView / JSP
```

建议顺序：

1. 先迁移低风险页面。
2. 保持原 URL 尽量不变。
3. 每迁移一个页面就验证一个页面。
4. 等 DAO / Service / Manager / Task 基本 Spring 化之后，再删除 Unidal MVC。

## 9. 不要做的事

下一次继续时注意：

1. 不要直接删除 `components.xml` 或 Plexus 注册。
2. 不要一次性把 `com.dianping.cat` 全量加入 Spring 扫描。
3. 不要让 Spring 和 Plexus 同时创建同一个会启动线程或注册定时任务的组件。
4. 不要直接用 Spring 新建 Processor 替换旧 Processor，除非确认字段依赖全部补齐。
5. 不要回滚用户已有改动。
6. 不要长期保留自己启动的 Java 进程。
7. 不要把空表、查无数据这类旧系统允许的场景误判成迁移失败。

## 10. 下一次任务开始建议执行的命令

先看工作树：

```powershell
git status --short
```

查看旧容器依赖剩余面：

```powershell
rg -n "ContainerLoader|getDefaultContainer|lookup\(" cat-core/src/main/java cat-home/src/main/java cat-alarm/src/main/java cat-consumer/src/main/java
rg -n "org\.unidal\.lookup\.annotation|@Named|Initializable|LogEnabled|ContainerHolder" cat-core/src/main/java cat-home/src/main/java cat-alarm/src/main/java cat-consumer/src/main/java
rg -n "DataSourceManager|MyBatisRepositorySupport" cat-core/src/main/java cat-home/src/main/java cat-alarm/src/main/java cat-consumer/src/main/java
```

查看配置 Processor 当前状态：

```powershell
rg -n "refreshSpringBeans|class .*ConfigProcessor|class BaseProcesser" cat-home/src/main/java/com/dianping/cat/system/page/config/processor cat-home/src/main/java/com/dianping/cat/system/page/config/Handler.java
```

然后继续执行第 6 节的配置 Processor Spring 桥接计划。
