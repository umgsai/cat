# CAT Bean 组件化迁移清单

## 1. 阶段目标

本文件对应第二阶段：建立迁移清单并选第一批 Bean。

本阶段仍不迁移代码，目标是先确定迁移顺序、风险边界和第一批 Bean 的具体动作。后续每批迁移都应回到本清单，按小批量推进。

## 2. 当前扫描约束

当前 `CatHomeSpringConfiguration` 的 `@ComponentScan` 不是默认包扫描，而是白名单扫描：

```text
basePackageClasses = Analyzer + ContainerMessageAnalyzerFactory
includeFilters = ASSIGNABLE_TYPE
useDefaultFilters = false
```

因此，后续给普通业务类加 `@Component` 后，如果不扩展扫描范围，注解不会生效。

第一批迁移时建议采用保守做法：

1. 不直接扫描整个 `com.dianping.cat`。
2. 只扩展第一批候选所在包，或继续使用明确的 include filter。
3. 每批迁移后确认配置类中的旧 `@Bean` 方法已经删除，避免同名或同类型 Bean 重复注册。

## 3. 清单分类

当前 `CatHomeSpringConfiguration` 中共有 377 个 `@Bean`。

按迁移风险粗分：

```text
aggregate：8
alarm：25
helper-builder：53
infra：5
manager：61
mvc-handler：57
other：27
prototype-report-manager：11
repository：24
service：58
storage-infra：14
task-runtime：34
```

各类处理策略：

| 分类 | 策略 |
| --- | --- |
| infra | 暂不迁移，继续由配置类显式创建 |
| aggregate | 暂不迁移，Map/List/Set 聚合仍由配置类集中装配 |
| repository | 暂缓，等 MyBatis/Repository 扫描策略稳定后再批量处理 |
| prototype-report-manager | 暂缓，必须单独处理 prototype 语义 |
| storage-infra | 暂缓，生命周期和文件存储风险较高 |
| task-runtime | 暂缓，涉及线程、checkpoint、task consumer、重复消费风险 |
| mvc-handler | 暂缓，继续跟 Spring MVC 页面迁移节奏走 |
| helper-builder | 优先迁移低风险类 |
| service/manager | 先迁移无生命周期、无复杂状态、依赖清晰的类 |
| alarm | 谨慎迁移，避免告警链路和聚合注册重复 |

## 4. 第一批候选

第一批选择业务图表和基线相关的 8 个 Bean。它们没有 `initMethod`，不是 prototype，不启动后台线程，不属于聚合 Bean，适合验证迁移模式。

状态：已完成，完成时间 2026-06-27。

| Bean | 当前行号 | 类 | 风险 | 迁移动作 |
| --- | ---: | --- | --- | --- |
| `businessKeyHelper` | 3871 | `com.dianping.cat.report.page.business.task.BusinessKeyHelper` | 低 | 已加 `@Component`；默认 Bean 名称保持 `businessKeyHelper` |
| `businessDataFetcher` | 3876 | `com.dianping.cat.report.page.business.graph.BusinessDataFetcher` | 低 | 已加 `@Component`；`BusinessKeyHelper` 已改 `@Resource` 注入；`m_keyHelper` 已改为 `businessKeyHelper` |
| `customDataCalculator` | 3925 | `com.dianping.cat.report.page.business.graph.CustomDataCalculator` | 低 | 已加 `@Component`；`BusinessKeyHelper` 已改 `@Resource` 注入；`m_keyHelper` 已改为 `businessKeyHelper`；已补充异常日志上下文 |
| `businessPointParser` | 3933 | `com.dianping.cat.report.page.business.task.BusinessPointParser` | 低 | 已加 `@Component`；单小时解析异常已补 debug 日志，避免高频刷屏 |
| `baselineConfigManager` | 3938 | `com.dianping.cat.report.page.metric.task.BaselineConfigManager` | 低 | 已加 `@Component`；无依赖 |
| `baselineCreator` | 3943 | `com.dianping.cat.report.page.metric.task.DefaultBaselineCreator` | 低 | 已加 `@Component("baselineCreator")`，保留当前 Bean 名称；实现类型仍是 `BaselineCreator` |
| `cachedBusinessReportService` | 3906 | `com.dianping.cat.report.page.business.service.CachedBusinessReportService` | 中低 | 已加 `@Component`；`BusinessReportService` 和 `businessModelService` 已改 `@Resource` 注入；缓存和依赖字段已重命名 |
| `businessReportGroupService` | 3916 | `com.dianping.cat.report.alert.business.BusinessReportGroupService` | 中低 | 已加 `@Component`；`businessModelService` 已用 `@Resource(name = "businessModelService")` 注入；`m_service` 已改为 `businessModelService` |

第一批删除的配置类方法：

```text
businessKeyHelper()
businessDataFetcher(...)
cachedBusinessReportService(...)
businessReportGroupService(...)
customDataCalculator(...)
businessPointParser()
baselineConfigManager()
baselineCreator()
```

状态：上述 8 个 `@Bean` 方法已从 `CatHomeSpringConfiguration` 删除。

## 5. 第一批不包含的相邻 Bean

这些 Bean 暂不放入第一批：

| Bean | 暂缓原因 |
| --- | --- |
| `businessGraphCreator` | 依赖较多，内部有多处异常处理和图表逻辑，适合作为第二批 |
| `businessAlert` | 告警链路，依赖聚合、规则、联系人、baseline，风险高于第一批 |
| `businessBaselineReportBuilder` | `TaskBuilder`，属于任务构建链路，暂不碰后台任务 |
| `businessReportService` | 继承报告服务链路，读写多张报表表，适合单独验证 |
| `baselineService` | 涉及数据库、缓存和异常降级，后续单独处理日志和命名 |

## 6. 第一批迁移注意点

### 6.1 Bean 名称

除 `baselineCreator` 外，第一批类的默认 Spring Bean 名称都与当前 `@Bean` 方法名一致。

`DefaultBaselineCreator` 的默认名称会是 `defaultBaselineCreator`，而当前配置类方法名是 `baselineCreator`，因此必须显式写：

```java
@Component("baselineCreator")
public class DefaultBaselineCreator implements BaselineCreator {
}
```

### 6.2 同类型注入

`ModelService<BusinessReport>` 在配置类中有多个 Bean，包括 historical、local、composite 等。迁移 `CachedBusinessReportService` 和 `BusinessReportGroupService` 时必须按名称注入：

```java
@Resource(name = "businessModelService")
private ModelService<BusinessReport> businessModelService;
```

不能只按类型注入。

### 6.3 命名收口

第一批需要处理的明显旧式命名：

```text
m_keyHelper       -> businessKeyHelper
m_service         -> businessModelService
m_reportService   -> businessReportService
m_businessReports -> businessReports
m_datas           -> dataByKey 或 graphData
m_domain          -> domain
strs              -> parts
```

只处理候选类和候选类内部的直接字段，不扩散到 DTO、Payload、JSP Model、枚举字段。

### 6.4 日志补强

第一批需要重点看这些位置：

1. `BusinessDataFetcher.buildGraphData(...)`：当前 null report 会记录 error 但后续仍可能继续访问 null，应在迁移时明确行为并补充 domain/report 上下文。
2. `CustomDataCalculator.calculate(...)`：当前 `JexlException` 被静默忽略，普通异常只 `Cat.logError(e)`，应补充 SLF4J debug/warn/error 上下文。
3. `BusinessPointParser.buildDailyData(...)`：当前单小时解析异常直接 continue，应按影响补充 debug 或 warn，避免高频刷屏。
4. `CachedBusinessReportService.queryBusinessReport(...)`：当没有可用 model service 时，异常信息可保留，但字段命名和注入要清晰。
5. `BusinessReportGroupService.fetchMetricReport(...)`：查询不到 report 返回 null 是业务语义，不需要打 error；如果后续捕获异常，应补充 domain、period、minute 范围。

### 6.5 行为不顺手改

迁移时不要顺手修改业务语义，除非编译或注入必须修改。

特别注意：

1. `BusinessReportGroupService` 中 `requireAll` 当前值是 `"ture"`，疑似历史拼写问题，但第一批迁移不主动修改，除非单独确认。
2. `DefaultBaselineCreator.computeAvg(...)` 会原地排序传入列表，第一批不改算法。
3. `CachedBusinessReportService` 的缓存仍保持 singleton 行为，与当前 `@Bean` 默认单例一致。

## 7. 第一批执行顺序

建议按以下顺序执行：

1. 扩展 Spring 扫描范围，仅覆盖第一批候选包或候选类。
2. 迁移无依赖 Bean：`BusinessKeyHelper`、`BusinessPointParser`、`BaselineConfigManager`、`DefaultBaselineCreator`。
3. 迁移单依赖 Bean：`BusinessDataFetcher`、`CustomDataCalculator`。
4. 迁移服务 Bean：`CachedBusinessReportService`、`BusinessReportGroupService`。
5. 删除 `CatHomeSpringConfiguration` 中对应 8 个 `@Bean` 方法。
6. 清理配置类中不再使用的 import。
7. 执行编译：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

8. 如编译通过，再 review 未提交改动，重点确认没有双注册、没有扫描过宽、没有无关格式化。

## 8. 第一批验收标准

第一批完成后应满足：

1. `cat-home` 编译通过。已验证：`mvn -pl cat-home -am -DskipTests compile`，结果 `BUILD SUCCESS`。
2. Spring 启动时没有同名 Bean 冲突。编译已通过；用户已确认应用可以正常启动。
3. `businessKeyHelper`、`businessDataFetcher`、`customDataCalculator`、`businessPointParser`、`baselineConfigManager`、`baselineCreator`、`cachedBusinessReportService`、`businessReportGroupService` 都由组件扫描创建。已通过白名单 include filter 纳入扫描。
4. `CatHomeSpringConfiguration` 中不再保留这 8 个 Bean 的 `@Bean` 方法。已完成。
5. 触碰类中的旧式 `m_` 字段已改为标准 Java 驼峰命名。已完成。
6. 触碰到的异常处理位置有 SLF4J 上下文日志。已补充 `BusinessDataFetcher`、`CustomDataCalculator`、`BusinessPointParser` 的日志。
7. 不影响业务页面和告警链路的现有行为。代码层面未主动修改算法和业务语义；用户已确认应用可以正常启动。

## 9. 第一批完成记录

完成内容：

1. `CatHomeSpringConfiguration` 继续使用白名单扫描，只把第一批 8 个类加入 `ASSIGNABLE_TYPE` include filter。
2. 第一批 8 个类已改为 `@Component` 创建。
3. 第一批依赖已改为 `jakarta.annotation.Resource` 字段注入。
4. `DefaultBaselineCreator` 显式使用 `@Component("baselineCreator")`，避免 Bean 名从 `baselineCreator` 变成 `defaultBaselineCreator`。
5. 两个 `ModelService<BusinessReport>` 依赖显式使用 `@Resource(name = "businessModelService")`，避免同类型注入歧义。
6. 删除配置类中对应 8 个 `@Bean` 方法。
7. 本批旧式字段命名已收口，例如 `m_keyHelper`、`m_service`、`m_reportService`、`m_businessReports`。
8. 补充了本批触碰异常位置的 SLF4J 日志。

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 24. 第十六批完成记录

第十六批按新的大批量计划迁移剩余页面 `Handler`，覆盖报表页和系统页中依赖较多的控制类。迁移原则仍然是只迁移 `Handler` 本身，暂不迁移 `TaskBuilder`、`ReportService`、`ModelService`、带 `initMethod` 的 Manager、Map/List 聚合 Bean 和 processor 链路。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 18 个 Bean 已改为 `@Component("原Bean名")` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
modelHandler -> com.dianping.cat.report.page.model.Handler
cacheHandler -> com.dianping.cat.report.page.cache.Handler
eventHandler -> com.dianping.cat.report.page.event.Handler
transactionHandler -> com.dianping.cat.report.page.transaction.Handler
problemHandler -> com.dianping.cat.report.page.problem.Handler
heartbeatHandler -> com.dianping.cat.report.page.heartbeat.Handler
businessHandler -> com.dianping.cat.report.page.business.Handler
logviewHandler -> com.dianping.cat.report.page.logview.Handler
topHandler -> com.dianping.cat.report.page.top.Handler
stateHandler -> com.dianping.cat.report.page.state.Handler
storageHandler -> com.dianping.cat.report.page.storage.Handler
dependencyHandler -> com.dianping.cat.report.page.dependency.Handler
statisticsHandler -> com.dianping.cat.report.page.statistics.Handler
matrixHandler -> com.dianping.cat.report.page.matrix.Handler
crossHandler -> com.dianping.cat.report.page.cross.Handler
systemConfigHandler -> com.dianping.cat.system.page.config.Handler
systemRouterHandler -> com.dianping.cat.system.page.router.Handler
systemBusinessHandler -> com.dianping.cat.system.page.business.Handler
```

2. 已删除 `CatHomeSpringConfiguration` 中对应 18 个 `@Bean` 工厂方法，避免和组件扫描生成的同名 Bean 冲突。

3. 本批所有迁移类都使用 `@Resource` 字段注入；泛型服务和聚合服务使用显式名称绑定，例如：

```text
@Resource(name = "transactionModelService")
@Resource(name = "eventModelService")
@Resource(name = "problemModelService")
@Resource(name = "heartbeatModelService")
@Resource(name = "topModelService")
@Resource(name = "stateModelService")
@Resource(name = "storageModelService")
@Resource(name = "dependencyModelService")
@Resource(name = "matrixModelService")
@Resource(name = "crossModelService")
@Resource(name = "logviewModelService")
@Resource(name = "localModelServices")
```

4. 触碰到的旧式字段命名已收口为 Java 驼峰命名，例如：

```text
m_jspViewer -> jspViewer
m_reportService -> transactionReportService / eventReportService / problemReportService 等具体名称
m_service -> transactionModelService / eventModelService / storageModelService 等具体名称
m_configManager -> domainGroupConfigManager / serverConfigManager / exceptionRuleConfigManager 等具体名称
m_mergeHelper -> transactionMergeHelper / eventMergeHelper / storageMergeHelper
m_jsonBuilder -> jsonBuilder
m_alertInfoBuilder -> storageAlertInfoBuilder
m_routerConfigHandler -> routerConfigHandler
m_tagConfigManger -> businessTagConfigManager
```

5. `systemConfigHandler` 中原来只调用 `Cat.logError` 的配置修改记录异常路径，已补充 SLF4J 日志，便于从应用日志直接定位用户、账号、动作和 cookie 解析问题。

6. 本批保留以下已有 Bean 的配置类注册，不在本批扩大范围：

```text
heartbeatHistoryGraphs
stateGraphBuilder
stateBuilder
TopologyGraphManager
RouterConfigManager
RouterConfigHandler
各类 ReportService / ModelService
各类 TaskBuilder
system config processor 链路
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

## 38. 第三十批完成记录

第三十批迁移告警汇总、远端服务缓存更新和规则模板渲染相关 Bean，目标是在不触碰 `ModelService`、存储和数据源链路的前提下，继续减少 `CatHomeSpringConfiguration` 中的显式 `@Bean` 注册数量。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
RelatedSummaryBuilder
FailureSummaryBuilder
AlterationSummaryBuilder
AlertSummaryExecutor
RemoteServersManager
DefaultRemoteServersUpdater
ServersUpdaterManager
RuleFTLDecorator
```

2. 原先带 `initMethod = "initialize"` 的类已改为 `@PostConstruct`，保留启动初始化语义：

```text
SummaryBuilder
ServersUpdaterManager
RuleFTLDecorator
```

3. 带旧 Bean 名语义的类已保留原名称：

```text
RelatedSummaryBuilder    -> AlertSummaryContentGenerator
FailureSummaryBuilder    -> FailureDecorator
AlterationSummaryBuilder -> AlterationSummaryContentGenerator
DefaultRemoteServersUpdater -> remoteServersUpdater
RuleFTLDecorator -> ruleFTLDecorator
```

4. 已删除 `CatHomeSpringConfiguration` 中对应旧 `@Bean` 方法，保留 `ReportManager`、`ModelService`、存储和数据源相关配置不动。
5. 本批触碰字段已按 Java 驼峰命名收口，并使用 `@Resource` 字段注入；原 setter 保留给测试和少量手工构造场景使用。
6. `DefaultRemoteServersUpdater` 继续使用 `@Resource(name = "localStateService")` 和 `@Resource(name = "stateModelService")` 精确注入，避免 `ModelService` 泛型擦除后的歧义。
7. `AlertSummaryExecutor` 继续按旧 Builder 名称注入三类 `SummaryBuilder`，保持汇总内容生成顺序不变。
8. 本批仍不迁移以下内容：

```text
ReportManager / ModelService / LocalModelService
ServerConfigManager
存储 bucket / HDFS / message dump 链路
DataSource / SqlSessionFactory / TransactionTemplate
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

## 39. 第三十一批完成记录

第三十一批迁移报表保存链路中的 `ReportDelegate` 及相邻的 `StorageReportUpdater`，目标是在不触碰 `ReportManager` 生命周期的前提下，继续减少 `CatHomeSpringConfiguration` 中的显式 `@Bean` 注册数量。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 `ReportDelegate` 已改为 `@Component("...Delegate")` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
BusinessDelegate
TransactionDelegate
CrossDelegate
DependencyDelegate
EventDelegate
HeartbeatDelegate
MatrixDelegate
ProblemDelegate
StorageDelegate
TopDelegate
StateDelegate
```

2. `StorageReportUpdater` 已改为 `@Component` 创建，并加入白名单扫描；`StorageDelegate` 通过 `@Resource` 注入它。
3. 已删除 `CatHomeSpringConfiguration` 中对应 11 个 `ReportDelegate` 旧 `@Bean` 方法，以及 `StorageReportUpdater` 旧 `@Bean` 方法。
4. `ReportManager` 仍保留在 `CatHomeSpringConfiguration` 中，并在构造参数上使用明确的 `@Qualifier("...Delegate")`，避免多个 `ReportDelegate` Bean 因泛型擦除导致注入歧义。
5. 本批触碰到的注入字段和普通状态字段已按 Java 驼峰命名收口，例如：

```text
m_taskManager                -> taskManager
m_configManager              -> serverFilterConfigManager
m_serverFilterConfigManager  -> serverFilterConfigManager
m_transactionManager         -> allReportConfigManager
m_allManager                 -> allReportConfigManager
m_serverConfigManager        -> serverConfigManager
m_atomicMessageConfigManager -> atomicMessageConfigManager
m_computer                   -> transactionStatisticsComputer / eventTpsStatisticsComputer
m_reportUpdater              -> storageReportUpdater
m_bucketManager              -> reportBucketManager
```

6. `StorageReportUpdater.StorageUpdateItem` 内部字段也已从 `m_` 命名改为驼峰命名。
7. 对本批触碰且原先只调用 `Cat.logError` 的异常路径补充了 SLF4J 日志，覆盖 `EventDelegate#createAggregatedReport` 和 `ProblemDelegate#beforeSave`。
8. 本批仍不迁移以下内容：

```text
ReportManager / ReportDelegate 聚合之外的 ModelService
ServerConfigManager
存储 bucket / HDFS / message dump 链路
DataSource / SqlSessionFactory / TransactionTemplate
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

## 38. 第三十批完成记录

第三十批迁移 `BusinessConfigManager` 和小时报表 reload 链路中的低风险 `ReportReloader` Bean，目标是继续减少 `CatHomeSpringConfiguration` 中的显式注册，同时保留原有 Bean 名称和初始化语义。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. `BusinessConfigManager` 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描。
2. `BusinessConfigManager` 原 `initMethod = "initialize"` 已改为 `@PostConstruct`，保留启动加载业务配置和注册 `TimerSyncTask` 的语义。
3. `BusinessConfigManager` 中的 Spring 注入字段已改为 `@Resource` 字段注入，并按 Java 驼峰命名收口：

```text
m_configDao           -> businessConfigRepository
m_serverConfigManager -> serverConfigManager
m_domains             -> domains
m_configs             -> configs
m_alertMachine        -> alertMachine
m_initialized         -> initialized
```

4. 以下 `ReportReloader` 已改为 `@Component("...ReportReloader")` 创建，并保留原 `@Bean` 方法名作为 Bean 名称：

```text
BusinessReportReloader
TransactionReportReloader
CrossReportReloader
DependencyReportReloader
EventReportReloader
HeartbeatReportReloader
MatrixReportReloader
ProblemReportReloader
StorageReportReloader
TopReportReloader
StateReportReloader
```

5. `AbstractReportReloader` 中的公共依赖已改为 `@Resource` 字段注入：

```text
hourlyReportRepository
hourlyReportContentRepository
serverConfigManager
```

6. 各具体 `ReportReloader` 的 `ReportManager` 使用明确 Bean 名注入，例如 `@Resource(name = TransactionAnalyzer.ID + "ReportManager")`，避免同类型泛型擦除后按类型注入不明确。
7. 各具体 `ReportReloader` 中的 `m_reportManager` 已按报告类型重命名为驼峰字段，例如 `transactionReportManager`、`eventReportManager`、`stateReportManager`。
8. 已删除 `CatHomeSpringConfiguration` 中 11 个旧 `ReportReloader` `@Bean` 方法、`configureReportReloader` helper，以及旧 `BusinessConfigManager` `@Bean` 方法。
9. `reportReloaders` 聚合 Map 仍保留在 `CatHomeSpringConfiguration` 中，继续通过旧 Bean 名称组装，避免影响 `ReportReloadTask` 的运行入口。
10. 本批仍不迁移以下内容：

```text
ServerConfigManager
ReportManager / ReportDelegate / ModelService
存储 bucket / HDFS / message dump 链路
DataSource / SqlSessionFactory / TransactionTemplate
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

## 30. 第二十二批完成记录

第二十二批扩大到告警任务层和告警配置 Manager 层。第二十一批已经迁移告警编排层，本批继续把具体告警任务和规则配置读取类从 `CatHomeSpringConfiguration` 的显式 `@Bean` 注册迁移为组件扫描注册。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下告警任务类已经改为 `@Component` 注册，并加入 `CatHomeSpringConfiguration` 的保守扫描白名单：

```text
BusinessAlert
EventAlert
ExceptionAlert
HeartbeatAlert
TransactionAlert
```

2. 以下规则/配置 Manager 已经改为 `@Component` 注册；原来依赖 `initMethod = "initialize"` 的类改为在 `initialize()` 上使用 `@PostConstruct`：

```text
BaseRuleConfigManager
TransactionRuleConfigManager
EventRuleConfigManager
HeartbeatRuleConfigManager
BusinessRuleConfigManager
ExceptionRuleConfigManager
BusinessTagConfigManager
AlertConfigManager
AlertPolicyManager
SenderConfigManager
```

3. 已删除 `CatHomeSpringConfiguration` 中对应的旧 `@Bean` 工厂方法，避免组件扫描后出现重复 Bean。

4. 本批触碰到的旧式字段命名已经收口为 Java 驼峰命名，并改为 `@Resource` 字段注入。存在同类型多 Bean 的依赖使用显式名称，避免注入歧义：

```text
spiAlertManager
eventModelService
transactionModelService
heartbeatModelService
topModelService
```

5. 本批未运行告警任务的真实循环测试，避免触发实际告警发送和分钟级后台循环；通过编译和静态 diff 检查验证迁移结果。

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

## 29. 第二十一批完成记录

第二十一批迁移告警编排层 Bean。前两批已经完成告警发送链路的叶子实现和 Manager 聚合层迁移，本批继续迁移 `com.dianping.cat.alarm.spi.AlertManager` 和 `com.dianping.cat.report.alert.AlarmManager`，但暂不迁移具体告警任务类 `BusinessAlert`、`EventAlert`、`ExceptionAlert`、`HeartbeatAlert`、`TransactionAlert`，避免同时改动告警扫描线程的业务依赖。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. `com.dianping.cat.alarm.spi.AlertManager` 已改为 `@Component("spiAlertManager")` 创建，并加入 `CatHomeSpringConfiguration` 的保守白名单扫描。
2. 已删除 `CatHomeSpringConfiguration` 中的 `spiAlertManager(...)` `@Bean(initMethod = "initialize")` 方法。
3. `AlertManager` 的依赖已改为 `@Resource` 字段注入：

```text
SpliterManager
SenderManager
AlertService
AlertPolicyManager
DecoratorManager
ContactorManager
ServerConfigManager
```

4. `AlertManager` 原 `initMethod = "initialize"` 生命周期已改为 `@PostConstruct`，保持发送线程和恢复通知线程启动行为。
5. `AlertManager` 中旧式 `m_` 字段已改为 Java 驼峰命名：

```text
m_initialized       -> initialized
m_splitterManager   -> spliterManager
m_senderManager     -> senderManager
m_alertService      -> alertService
m_policyManager     -> alertPolicyManager
m_decoratorManager  -> decoratorManager
m_contactorManager  -> contactorManager
m_configManager     -> serverConfigManager
m_alerts            -> alerts
m_unrecoveredAlerts -> unrecoveredAlerts
m_sendedAlerts      -> sentAlerts
m_alertMap          -> alertMap
m_sdf               -> sdf
```

6. `com.dianping.cat.report.alert.AlarmManager` 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 的保守白名单扫描。
7. 已删除 `CatHomeSpringConfiguration` 中的 `alarmManager(...)` `@Bean` 方法。
8. `AlarmManager` 中的具体告警任务依赖已改为 `@Resource` 字段注入，字段命名同步改为驼峰格式。
9. `AlertManager` 和 `AlarmManager` 的 setter 方法暂时保留，兼容测试或少量手工装配场景；主路径已由 Spring 注解注入。
10. 本批未运行 `AlertTest`、`SuspendTest`、`SenderTest`、`SenderManagerTest`，因为它们会触发告警巡检、真实发送链路或长时间等待。

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

## 28. 第二十批完成记录

第二十批承接第十九批的告警发送链路改造，迁移告警聚合层 Manager。第十九批已经将 sender、spliter、contactor、decorator 的叶子实现改为组件注册；本批进一步将负责聚合调用的 Manager 改为 `@Component` 注册。`alertSenders`、`alertSpliters`、`alertContactors`、`alertDecorators` 这 4 个 Map 聚合 Bean 暂时继续保留在 `CatHomeSpringConfiguration` 中，作为稳定的命名装配点。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Manager 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 的保守白名单扫描：

```text
SenderManager
SpliterManager
ContactorManager
DecoratorManager
```

2. 已删除 `CatHomeSpringConfiguration` 中上述 4 个 Manager 的 `@Bean(initMethod = "initialize")` 方法。
3. 4 个 Manager 中的旧式 `m_` 字段已改为 Java 驼峰命名：

```text
SenderManager:
m_configManager -> serverConfigManager
m_senders       -> senders
m_initialized   -> initialized

SpliterManager:
m_spliters      -> spliters
m_initialized   -> initialized

ContactorManager:
m_contactors    -> contactors
m_initialized   -> initialized

DecoratorManager:
m_decorators    -> decorators
m_initialized   -> initialized
```

4. 4 个 Manager 的依赖已改为 `@Resource` 字段注入：

```text
SenderManager     -> @Resource ServerConfigManager
SenderManager     -> @Resource(name = "alertSenders")
SpliterManager    -> @Resource(name = "alertSpliters")
ContactorManager  -> @Resource(name = "alertContactors")
DecoratorManager  -> @Resource(name = "alertDecorators")
```

5. 原 `initMethod = "initialize"` 生命周期已改为 `@PostConstruct`，保持初始化日志和空配置告警行为。
6. 旧 setter 方法暂时保留，兼容测试或少量手工装配场景；主路径已由 Spring 注解注入。
7. 本批没有迁移 4 个 Map 聚合 Bean，避免同时改变聚合装配方式和 Manager 生命周期。
8. `SenderManagerTest`、`SenderTest`、`AlertTest`、`SuspendTest` 存在真实发送、告警巡检或长时间等待等副作用，本批未作为自动化验证运行。

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

## 27. 第十九批完成记录

第十九批迁移告警发送链路中的叶子 Bean，范围控制在 sender、spliter、contactor、decorator 的具体实现类。`SenderManager`、`SpliterManager`、`ContactorManager`、`DecoratorManager` 以及 `alertSenders`、`alertSpliters`、`alertContactors`、`alertDecorators` 这些聚合 Bean 暂时保留在 `CatHomeSpringConfiguration` 中，避免一次性扩大聚合关系改造范围。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 sender 已改为 `@Component` 创建，并通过组件扫描白名单注册：

```text
mailSender
smsSender
weixinSender
```

2. 以下 spliter 已改为 `@Component` 创建，并通过组件扫描白名单注册：

```text
mailSpliter
smsSpliter
weixinSpliter
dxSpliter
```

3. 以下 contactor 已改为 `@Component` 创建，并通过组件扫描白名单注册：

```text
businessContactor
eventContactor
exceptionContactor
heartbeatContactor
transactionContactor
```

4. 以下 decorator 已改为 `@Component` 创建，并通过组件扫描白名单注册：

```text
businessDecorator
eventDecorator
exceptionDecorator
heartbeatDecorator
transactionDecorator
```

5. 已删除 `CatHomeSpringConfiguration` 中上述叶子 Bean 的 `@Bean` 方法，保留聚合 Map 和 Manager 的配置方法。
6. `AbstractSender`、`ProjectContactor`、`ProjectDecorator` 的注入字段已从 `m_` 风格调整为 Java 驼峰命名，并使用 `@Resource` 注入。
7. `BusinessDecorator`、`ExceptionDecorator` 中的 `m_executor` 已重命名为 `alertSummaryExecutor`，并使用 `@Resource` 注入。
8. 原先依赖 `@Bean(initMethod = "initialize")` 的 `EventDecorator`、`ExceptionDecorator`、`TransactionDecorator` 已补充 `@PostConstruct`，保持初始化行为不变。
9. `SenderTest`、`SenderManagerTest` 会实际触发邮件/微信/短信发送逻辑，本批未作为自动化验证运行，避免误发外部告警。

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

运行时验证：

```text
用户已确认应用可以正常编译并启动。
```

## 25. 第十七批完成记录

第十七批扩大范围迁移页面支撑类和 system config processor 链路。计划中部分辅助类（`DependencyItemBuilder`、`TopologyGraphBuilder`、`StorageAlertInfoBuilder`、`ExternalInfoBuilder`、`StorageMergeHelper`、`EventMergeHelper`、`TransactionMergeHelper`）此前已经完成组件化并在扫描白名单中，本批没有重复改动。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 12 个 Bean 已改为 `@Component("原Bean名")` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
heartbeatHistoryGraphs -> com.dianping.cat.report.page.heartbeat.HistoryGraphs
stateGraphBuilder -> com.dianping.cat.report.page.state.StateGraphBuilder
stateBuilder -> com.dianping.cat.report.page.state.StateBuilder
configHtmlParser -> com.dianping.cat.system.page.config.ConfigHtmlParser
globalConfigProcessor -> com.dianping.cat.system.page.config.processor.GlobalConfigProcessor
dependencyConfigProcessor -> com.dianping.cat.system.page.config.processor.DependencyConfigProcessor
exceptionConfigProcessor -> com.dianping.cat.system.page.config.processor.ExceptionConfigProcessor
heartbeatConfigProcessor -> com.dianping.cat.system.page.config.processor.HeartbeatConfigProcessor
storageConfigProcessor -> com.dianping.cat.system.page.config.processor.StorageConfigProcessor
transactionConfigProcessor -> com.dianping.cat.system.page.config.processor.TransactionConfigProcessor
eventConfigProcessor -> com.dianping.cat.system.page.config.processor.EventConfigProcessor
alertConfigProcessor -> com.dianping.cat.system.page.config.processor.AlertConfigProcessor
```

2. 已删除 `CatHomeSpringConfiguration` 中对应 12 个 `@Bean` 工厂方法，继续保留 `RuleFTLDecorator`、`RouterConfigManager`、`TopologyGraphManager` 等带初始化或生命周期语义的 Bean。

3. 本批所有迁移类都使用 `@Resource` 字段注入；其中有歧义风险的依赖使用显式名称，例如：

```text
StateBuilder: @Resource(name = "stateModelService")
BaseProcesser: @Resource(name = "ruleFTLDecorator")
```

4. 触碰到的旧式字段命名已收口为 Java 驼峰命名，例如：

```text
m_reportService -> heartbeatReportService / stateReportService
m_manager -> heartbeatDisplayPolicyManager
m_routerManager -> routerConfigManager
m_stateService -> stateModelService
m_projectService -> projectService
m_domainGroupConfigManger -> domainGroupConfigManager
m_transactionConfigManager -> allReportConfigManager
m_reloadConfigManager -> reportReloadConfigManager
m_ruleDecorator -> ruleDecorator
m_configManager -> transactionRuleConfigManager / eventRuleConfigManager
```

5. `BaseProcesser` 原先只调用 `Cat.logError` 或直接吞掉的规则新增、更新、删除异常路径，已补充 SLF4J 日志；`GlobalConfigProcessor.queryAllProjects()` 也补充了查询失败日志。

6. 本批仍不迁移以下内容：

```text
带 initMethod 的 Manager
ReportService / ModelService
TaskBuilder
ReportManager
Repository / DataSource / TransactionTemplate
后台线程和调度类
Map/List 聚合 Bean
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

## 11. 第三批完成记录

第三批选择工具/适配类 Bean，目标是迁移无后台线程、无 prototype、无复杂生命周期的通用组件。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
jsonBuilder -> com.dianping.cat.helper.JsonBuilder
payloadNormalizer -> com.dianping.cat.mvc.PayloadNormalizer
reportModelDependencies -> com.dianping.cat.mvc.ReportModelDependencies
valueTranslater -> com.dianping.cat.report.graph.svg.DefaultValueTranslater
graphBuilder -> com.dianping.cat.report.graph.svg.DefaultGraphBuilder
```

2. 已删除 `CatHomeSpringConfiguration` 中对应 5 个 `@Bean` 方法：

```text
payloadNormalizer(...)
reportModelDependencies(...)
jsonBuilder()
valueTranslater()
graphBuilder(...)
```

3. `DefaultValueTranslater` 使用 `@Component("valueTranslater")`，保留原 Bean 名。
4. `DefaultGraphBuilder` 使用 `@Component("graphBuilder")`，保留原 Bean 名。
5. `DefaultGraphBuilder` 中 `ValueTranslater` 已改为 `@Resource(name = "valueTranslater")` 字段注入。
6. `PayloadNormalizer` 中 `ServerConfigManager` 已改为 `@Resource` 字段注入。
7. `ReportModelDependencies` 中 `ProjectService`、`HostinfoService`、`SampleConfigManager` 已改为 `@Resource` 字段注入；getter 中保留原非空校验语义。
8. `JsonBuilder`、`DefaultGraphBuilder`、`PayloadNormalizer`、`ReportModelDependencies` 中本批触碰的旧式字段命名已改为 Java 驼峰命名。
9. `DefaultGraphBuilder#setGraphType(...)` 保持原空实现，不改变图表行为。
10. 因 `cat-core` 本批开始直接使用 `jakarta.annotation.Resource`，已在 `cat-core/pom.xml` 增加 `jakarta.annotation-api` 显式依赖。

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 12. 第四批完成记录

第四批选择 dependency 图构建辅助 Bean，继续避开有生命周期的 `TopologyGraphManager`。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
dependencyItemBuilder -> com.dianping.cat.report.page.dependency.graph.DependencyItemBuilder
topologyGraphBuilder -> com.dianping.cat.report.page.dependency.graph.TopologyGraphBuilder
```

2. 已删除 `CatHomeSpringConfiguration` 中对应 2 个 `@Bean` 方法：

```text
dependencyItemBuilder(...)
topologyGraphBuilder(...)
```

3. `DependencyItemBuilder` 中 `TopologyGraphConfigManager` 已改为 `@Resource` 字段注入。
4. `TopologyGraphBuilder` 中 `DependencyItemBuilder` 已改为 `@Resource` 字段注入。
5. 本批触碰到的旧式字段命名已改为 Java 驼峰命名：

```text
m_graphConfigManager -> topologyGraphConfigManager
m_itemBuilder        -> dependencyItemBuilder
m_domain             -> domain
m_graphs             -> graphs
m_minute             -> minute
m_date               -> date
m_pigeonServices     -> pigeonServiceTypes
```

6. `TopologyGraphBuilder#setItemBuilder(...)` 保留，用于兼容 `TopologyGraphManager` 中手动 `new TopologyGraphBuilder().setItemBuilder(...)` 的旧路径。
7. `TopologyGraphManager` 仍暂缓迁移，因为它有 `initMethod = "initialize"` 且依赖较多。

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 13. 第五批完成记录

第五批选择页面展示辅助 Bean，继续迁移无后台线程、无 `initMethod`、无聚合注册语义的低风险组件。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
storageAlertInfoBuilder -> com.dianping.cat.report.page.storage.display.StorageAlertInfoBuilder
externalInfoBuilder -> com.dianping.cat.report.page.dependency.ExternalInfoBuilder
```

2. 已删除 `CatHomeSpringConfiguration` 中对应 2 个 `@Bean` 方法：

```text
storageAlertInfoBuilder(...)
externalInfoBuilder(...)
```

3. `StorageAlertInfoBuilder` 中 `AlertService` 已改为 `@Resource` 字段注入。
4. `ExternalInfoBuilder` 中依赖已改为 `@Resource` 字段注入，其中 `ModelService<ProblemReport>` 使用 `@Resource(name = "problemModelService")`，避免同类型 Bean 注入歧义。
5. 本批触碰到的旧式字段命名已改为 Java 驼峰命名：

```text
m_alertService        -> alertService
m_sdf                 -> dateFormat
m_serverConfigManager -> serverConfigManager
m_problemservice      -> problemModelService
m_reportService       -> dependencyReportService
m_dateFormat          -> dateFormat
```

6. `StorageAlertInfoBuilder` 中原本只调用 `Cat.logError` 的告警时间异常分支，已补充 SLF4J warn 日志，包含 alert、alertDate、start、end、type 上下文。
7. `ExternalInfoBuilder` 中 problem model service 不可用的异常分支，已补充 SLF4J error 日志，包含 request 上下文。

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 14. 第六批完成记录

第六批选择 storage/cross 低风险辅助 Bean，继续避开聚合 Bean、`initMethod` Bean 和任务构建链路。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
storageMergeHelper -> com.dianping.cat.report.page.storage.transform.StorageMergeHelper
databaseParser -> com.dianping.cat.consumer.DatabaseParser
ipConvertManager -> com.dianping.cat.consumer.cross.IpConvertManager
storageSQLBuilder -> com.dianping.cat.consumer.storage.builder.StorageSQLBuilder
storageCacheBuilder -> com.dianping.cat.consumer.storage.builder.StorageCacheBuilder
storageRPCBuilder -> com.dianping.cat.consumer.storage.builder.StorageRPCBuilder
```

2. 已删除 `CatHomeSpringConfiguration` 中对应 6 个简单 `@Bean` 方法：

```text
storageMergeHelper()
databaseParser()
ipConvertManager()
storageSQLBuilder(...)
storageCacheBuilder()
storageRPCBuilder()
```

3. `StorageSQLBuilder` 中 `DatabaseParser` 已改为 `@Resource` 字段注入，并显式使用 `@Component("storageSQLBuilder")` 保持原 Bean 名称。
4. `StorageCacheBuilder`、`StorageRPCBuilder` 分别显式使用 `@Component("storageCacheBuilder")`、`@Component("storageRPCBuilder")`，保持现有 `@Qualifier` 注入语义。
5. 本批触碰到的旧式字段命名已改为 Java 驼峰命名：

```text
m_databaseParser  -> databaseParser
m_errorConnections -> errorConnections
m_connections     -> connections
m_hosts           -> hosts
```

6. `DatabaseParser` 中解析 JDBC 连接异常的分支，已补充 SLF4J warn 日志，包含 connection 上下文。
7. `IpConvertManager` 中 hostname 解析异常的分支，已补充 SLF4J warn 日志，包含 hostName 上下文。
8. 以下 Bean 本批继续保留在配置类中，避免改变聚合和生命周期语义：

```text
storageBuilders(...)
storageBuilderManager(...)
storageReportBuilder(...)
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 15. 第七批完成记录

第七批选择 ProblemHandler 小批量迁移，验证“命名 Bean + 保留默认配置值 + List 聚合暂留配置类”的迁移方式。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
default-problem -> com.dianping.cat.consumer.problem.DefaultProblemHandler
long-execution -> com.dianping.cat.consumer.problem.LongExecutionProblemHandler
```

2. 已删除 `CatHomeSpringConfiguration` 中对应 2 个简单 `@Bean` 方法：

```text
defaultProblemHandler()
longExecutionProblemHandler(...)
```

3. `DefaultProblemHandler` 使用 `@Component(DefaultProblemHandler.ID)` 保留原 Bean 名，并在类内保留默认错误类型：

```text
Error,RuntimeException,Exception
```

4. `LongExecutionProblemHandler` 使用 `@Component(LongExecutionProblemHandler.ID)` 保留原 Bean 名，`ServerConfigManager` 已改为 `@Resource` 字段注入。
5. `LongExecutionProblemHandler#setConfigManager(...)` 暂时保留，用于兼容现有单元测试和手动构造路径。
6. 本批触碰到的旧式字段命名已改为 Java 驼峰命名，并修正了原字段名拼写：

```text
m_errorTypes                -> errorTypes
m_configManager             -> serverConfigManager
m_defaultLongServiceDuration -> defaultLongServiceDuration
m_defaultLongSqlDuration    -> defaultLongSqlDuration
m_defaultLongUrlDuration    -> defaultLongUrlDuration
m_defalutLongCallDuration   -> defaultLongCallDuration
m_defaultLongCacheDuration  -> defaultLongCacheDuration
m_longServiceThresholds     -> longServiceThresholds
m_longSqlThresholds         -> longSqlThresholds
m_longUrlThresholds         -> longUrlThresholds
m_longCallThresholds        -> longCallThresholds
m_longCacheThresholds       -> longCacheThresholds
m_initialized               -> initialized
```

7. 以下聚合 Bean 本批继续保留在配置类中，避免改变 List 聚合语义：

```text
problemHandlers(...)
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 16. 第八批完成记录

第八批选择告警基础薄服务/工具类迁移，继续避开告警发送主链路、初始化 manager 和聚合 Bean。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
alertSummaryService -> com.dianping.cat.report.alert.summary.AlertSummaryService
alertService -> com.dianping.cat.alarm.service.AlertService
dataChecker -> com.dianping.cat.alarm.spi.rule.DefaultDataChecker
baseRuleHelper -> com.dianping.cat.report.alert.config.BaseRuleHelper
```

2. 已删除 `CatHomeSpringConfiguration` 中对应 4 个简单 `@Bean` 方法：

```text
alertSummaryService(...)
alertService(...)
dataChecker()
baseRuleHelper()
```

3. `AlertSummaryService` 中 `AlertSummaryRepository` 已改为 `@Resource` 字段注入。
4. `AlertService` 中 `AlertRepository` 已改为 `@Resource` 字段注入。
5. `DefaultDataChecker`、`BaseRuleHelper` 为无状态工具类，仅加 `@Component`。
6. `cat-alarm/pom.xml` 已显式增加 `spring-context` 和 `jakarta.annotation-api` 依赖，避免组件注解依赖传递依赖偶然可见。
7. 本批触碰到的旧式字段命名已改为 Java 驼峰命名：

```text
m_alertSummaryDao -> alertSummaryRepository
m_alertDao        -> alertRepository
```

8. 以下 Bean 本批继续保留在配置类中，避免改变初始化和告警链路语义：

```text
spiAlertManager(...)
AlarmManager
BusinessAlert/EventAlert/ExceptionAlert/HeartbeatAlert/TransactionAlert
RuleConfigManager 相关 Bean
SenderManager/ContactorManager/SpliterManager/DecoratorManager
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 17. 第九批完成记录

第九批选择告警摘要和规则辅助组件迁移，继续避开摘要 builder 初始化链路、摘要执行器和 `TopologyGraphManager` 本身。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
alertInfoBuilder -> com.dianping.cat.report.alert.summary.build.AlertInfoBuilder
userDefinedRuleManager -> com.dianping.cat.report.alert.spi.config.UserDefinedRuleManager
baselineService -> com.dianping.cat.report.page.metric.service.DefaultBaselineService
```

2. 已删除 `CatHomeSpringConfiguration` 中对应 3 个简单 `@Bean` 方法：

```text
alertInfoBuilder(...)
userDefinedRuleManager(...)
baselineService(...)
```

3. `AlertInfoBuilder` 中 `AlertRepository`、`TopologyGraphManager` 已改为 `@Resource` 字段注入。
4. `UserDefinedRuleManager` 中 `UserDefineRuleRepository` 已改为 `@Resource` 字段注入。
5. `DefaultBaselineService` 使用 `@Component("baselineService")`，保留原 Bean 名，避免从 `baselineService` 变成 `defaultBaselineService`。
6. `DefaultBaselineService` 中 `BaselineRepository` 已改为 `@Resource` 字段注入。
7. 本批触碰到的旧式字段命名已改为 Java 驼峰命名：

```text
m_alertDao        -> alertRepository
m_topologyManager -> topologyGraphManager
m_dao             -> userDefineRuleRepository
m_baselineDao     -> baselineRepository
m_baselines       -> baselines
m_empties         -> empties
```

8. `DefaultBaselineService` 中原本只调用 `Cat.logError` 的 baseline 查询、插入、解码异常分支，已补充 SLF4J 日志上下文。
9. 以下 Bean 本批继续保留在配置类中，避免改变初始化和聚合语义：

```text
RelatedSummaryBuilder/FailureSummaryBuilder/AlterationSummaryBuilder
AlertSummaryExecutor
TopologyGraphManager
RuleConfigManager 相关 Bean
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 18. 第十批完成记录

第十批选择报表页面/图表无状态辅助类迁移，继续避开 ReportService、ModelService、TaskBuilder 和聚合 Bean。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
dataExtractor -> com.dianping.cat.report.graph.metric.impl.DataExtractorImpl
eventMergeHelper -> com.dianping.cat.report.page.event.transform.EventMergeHelper
transactionMergeHelper -> com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper
```

2. 已删除 `CatHomeSpringConfiguration` 中对应 3 个简单 `@Bean` 方法：

```text
dataExtractor()
eventMergeHelper()
transactionMergeHelper()
```

3. `DataExtractorImpl` 使用 `@Component("dataExtractor")`，保留原 Bean 名，避免默认名变成 `dataExtractorImpl`。
4. `EventMergeHelper`、`TransactionMergeHelper` 为无依赖 helper，仅加 `@Component`。
5. 本批触碰到的旧式字段命名已改为 Java 驼峰命名：

```text
m_step -> step
```

6. 以下 Bean 本批继续保留在配置类中，避免改变数据库读写、任务和聚合语义：

```text
ReportService/ModelService 相关 Bean
TaskBuilder 相关 Bean
StateBuilder/StateGraphBuilder
RemoteServersManager/ServersUpdaterManager
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 19. 第十一批完成记录

第十一批选择无复杂生命周期的基础工具和告警展示辅助 Bean 迁移，继续避开 `initMethod`、后台线程、任务构建和聚合 Bean。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
contentFetcher -> com.dianping.cat.config.content.LocalResourceContentFetcher
pathBuilder -> com.dianping.cat.message.DefaultPathBuilder
serverStatisticManager -> com.dianping.cat.statistic.ServerStatisticManager
alertExceptionBuilder -> com.dianping.cat.report.alert.exception.AlertExceptionBuilder
```

2. 已删除 `CatHomeSpringConfiguration` 中对应 4 个简单 `@Bean` 方法：

```text
contentFetcher()
pathBuilder()
serverStatisticManager()
alertExceptionBuilder(...)
```

3. `LocalResourceContentFetcher` 使用 `@Component("contentFetcher")`，保留原 Bean 名。
4. `DefaultPathBuilder` 使用 `@Component("pathBuilder")`，保留原 Bean 名。
5. `ServerStatisticManager` 为统计状态单例，原配置类中也是单例，本批仅迁移注册方式。
6. `AlertExceptionBuilder` 中 `ExceptionRuleConfigManager` 已改为 `@Resource` 字段注入。
7. 本批触碰到的旧式字段命名已改为 Java 驼峰命名：

```text
m_serverState        -> serverState
m_currentStatistic   -> currentStatistic
m_currentMinute      -> currentMinute
m_exceptionConfigManager -> exceptionRuleConfigManager
```

8. `LocalResourceContentFetcher` 中原本 SLF4J warn 未打印异常对象，本批已补充异常堆栈，便于排查默认配置加载失败。
9. `DefaultMessageFinderManager` 本批暂缓迁移，因为它位于 `cat-hadoop` 模块，该模块当前未引入 Spring 依赖。

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 20. 第十二批完成记录

第十二批选择报表页面 Viewer 迁移，只处理无依赖的 `JspViewer` / `XmlViewer`，继续暂缓 Handler、System 页面 Viewer 和业务服务链路。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
homeJspViewer
monitorJspViewer
modelJspViewer
alterationJspViewer
alertJspViewer
cacheJspViewer
eventJspViewer
transactionJspViewer
transactionXmlViewer
problemJspViewer
heartbeatJspViewer
topJspViewer
businessJspViewer
logviewJspViewer
stateJspViewer
storageJspViewer
dependencyJspViewer
matrixJspViewer
statisticsJspViewer
overloadJspViewer
crossJspViewer
```

2. 已删除 `CatHomeSpringConfiguration` 中对应 21 个简单 `@Bean` 方法。
3. 所有 `JspViewer` 类名相同，因此均使用显式组件名，例如 `@Component("transactionJspViewer")`，避免默认 Bean 名 `jspViewer` 冲突。
4. `transactionXmlViewer` 使用 `@Component("transactionXmlViewer")` 保留原 Bean 名。
5. 本批仅迁移 Report 页面 Viewer，不迁移以下内容：

```text
Handler
System 页面 Viewer
ReportService/ModelService
TaskBuilder
ReportManager
带 initMethod 的 Manager
Map/List 聚合 Bean
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 21. 第十三批完成记录

第十三批选择 System 页面 Viewer 迁移，只处理无依赖的 `JspViewer`，继续暂缓 System Handler、配置 Processor 和 Manager 初始化链路。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
systemConfigJspViewer
systemBusinessJspViewer
systemPermissionJspViewer
systemLoginJspViewer
systemPluginJspViewer
systemProjectJspViewer
```

2. 已删除 `CatHomeSpringConfiguration` 中对应 6 个简单 `@Bean` 方法。
3. 所有 System 页面 Viewer 类名同为 `JspViewer`，因此均使用显式组件名，例如 `@Component("systemConfigJspViewer")`，避免默认 Bean 名 `jspViewer` 冲突。
4. 本批仅迁移 System 页面 Viewer，不迁移以下内容：

```text
System Handler
Report Handler
Processor
ReportService/ModelService
TaskBuilder
ReportManager
带 initMethod 的 Manager
Map/List 聚合 Bean
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 22. 第十四批完成记录

第十四批选择低依赖 Handler 迁移，验证 Handler 从配置类 setter 注入迁移到 `@Component` + `@Resource` 字段注入的路径。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
monitorHandler -> com.dianping.cat.report.page.monitor.Handler
overloadHandler -> com.dianping.cat.report.page.overload.Handler
systemLoginHandler -> com.dianping.cat.system.page.login.Handler
systemPluginHandler -> com.dianping.cat.system.page.plugin.Handler
systemProjectHandler -> com.dianping.cat.system.page.project.Handler
```

2. 已删除 `CatHomeSpringConfiguration` 中对应 5 个 `@Bean` 方法。
3. 所有 `Handler` 类名相同，因此均使用显式组件名，例如 `@Component("systemLoginHandler")`，避免默认 Bean 名 `handler` 冲突。
4. 以下依赖已改为 `@Resource` 字段注入：

```text
systemPluginHandler: JspViewer
systemLoginHandler: JspViewer, SigninService
systemProjectHandler: JspViewer, ProjectService
overloadHandler: JspViewer, TableCapacityService
```

5. 本批触碰到的旧式字段命名已改为 Java 驼峰命名：

```text
m_jspViewer            -> jspViewer
m_signinService        -> signinService
m_projectService       -> projectService
m_tableCapacityService -> tableCapacityService
m_serverMapping        -> serverMapping
```

6. 本批不迁移以下内容：

```text
依赖 ModelService/ReportService 的 Handler
依赖 Map/List 聚合 Bean 的 Handler
systemConfigHandler/systemBusinessHandler
top/storage/dependency 等复杂 Handler
TaskBuilder
ReportManager
带 initMethod 的 Manager
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 23. 第十五批完成记录

第十五批继续选择中低依赖 Handler 迁移，覆盖仓储、告警发送、配置展示和首页控制类，仍然避开 ModelService/ReportService 相关 Handler。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
homeHandler -> com.dianping.cat.report.page.home.Handler
alterationHandler -> com.dianping.cat.report.page.alteration.Handler
alertHandler -> com.dianping.cat.report.page.alert.Handler
systemPermissionHandler -> com.dianping.cat.system.page.permission.Handler
```

2. 已删除 `CatHomeSpringConfiguration` 中对应 4 个 `@Bean` 方法。
3. 所有 `Handler` 类名相同，因此均使用显式组件名，例如 `@Component("alterationHandler")`，避免默认 Bean 名 `handler` 冲突。
4. 以下依赖已改为 `@Resource` 字段注入：

```text
homeHandler: JspViewer, TcpSocketReceiver, MessageConsumer
alterationHandler: JspViewer, AlterationRepository
alertHandler: JspViewer, SenderManager, AlertRepository
systemPermissionHandler: JspViewer, UserConfigManager, ResourceConfigManager, ConfigHtmlParser
```

5. 本批触碰到的旧式字段命名已改为 Java 驼峰命名：

```text
m_jspViewer            -> jspViewer
m_receiver             -> tcpSocketReceiver
m_realtimeConsumer     -> messageConsumer
m_alterationDao        -> alterationRepository
m_sdf                  -> dateFormat
m_senderManager        -> senderManager
m_alertDao             -> alertRepository
m_userConfigManager    -> userConfigManager
m_resourceConfigManager -> resourceConfigManager
m_configHtmlParser     -> configHtmlParser
```

6. 本批不迁移以下内容：

```text
依赖 ModelService/ReportService 的 Handler
依赖 Map/List 聚合 Bean 的 Handler
systemConfigHandler/systemBusinessHandler
top/storage/dependency 等复杂 Handler
TaskBuilder
ReportManager
带 initMethod 的 Manager
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 10. 第二批完成记录

第二批选择 `BusinessGraphCreator` 一个 Bean，目标是验证依赖较多但不涉及后台线程、不涉及 prototype 的普通业务图表 Bean 迁移方式。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. `BusinessGraphCreator` 已加 `@Component`，由组件扫描创建。
2. `CatHomeSpringConfiguration` 的白名单扫描已加入 `BusinessGraphCreator.class`，未打开全包扫描。
3. 已删除 `CatHomeSpringConfiguration` 中的 `businessGraphCreator(...)` `@Bean` 方法。
4. `BusinessGraphCreator` 的 setter 注入已改为 `@Resource` 字段注入。
5. `BusinessGraphCreator` 中旧式字段命名已收口：

```text
m_reportService          -> cachedBusinessReportService
m_configManager          -> businessConfigManager
m_dataFetcher            -> businessDataFetcher
m_projectService         -> projectService
m_tagManager             -> businessTagConfigManager
m_keyHelper              -> businessKeyHelper
m_customDataCalculator   -> customDataCalculator
```

6. `AbstractGraphCreator` 中原先由 `BusinessGraphCreator` 配置方法间接注入的父类依赖已改为 `@Resource` 字段注入：

```text
m_baselineService -> baselineService
m_dataExtractor   -> dataExtractor
m_alertManager    -> alertManager，使用 @Resource(name = "spiAlertManager")
m_lastMinute      -> lastMinute
m_extraTime       -> extraTime
```

7. `BusinessGraphCreator` 原有 SLF4J + `Cat.logError` 异常日志保留；本批没有修改图表算法和业务语义。

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

## 26. 第十八批完成记录

第十八批聚焦 Spring MVC 迁移路径中的 Controller 和页面服务适配类。实际核对后发现，Spring MVC Controller 已经由 `SpringMvcMigrationConfiguration` 的 `@ComponentScan(basePackages = "com.dianping.cat.home.spring.web")` 扫描注册；`JsonBuilder`、`PayloadNormalizer`、`ReportModelDependencies`、`DefaultGraphBuilder`、`DefaultValueTranslater` 也已经是组件扫描白名单内的组件。本批因此只迁移仍由配置方法创建的低风险支撑 Bean，并对触碰到的 Spring MVC Controller 注入字段做命名规范化。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. `DomainValidator` 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 的保守白名单扫描。
2. 已删除 `CatHomeSpringConfiguration` 中的 `domainValidator()` `@Bean` 方法。
3. Spring MVC Controller 中触碰到的注入字段已从 `m_` 风格改为 Java 驼峰命名，覆盖：

```text
SpringMvcLoginController
SpringMvcProjectController
SpringMvcBusinessController
SpringMvcConfigController
SpringMvcRouterController
SpringMvcBusinessReportController
SpringMvcLogviewController
SpringMvcTransactionController
SpringMvcEventController
SpringMvcProblemController
SpringMvcHeartbeatController
SpringMvcCrossController
SpringMvcStateController
SpringMvcTopController
```

4. 多个 `ModelService` 注入点已从 `@Resource` + `@Qualifier` 调整为 `@Resource(name = "...")`，避免同类型 Bean 注入歧义。
5. `SpringMvcConfigController` 中原先直接吞掉异常并返回 `false` 的配置写入路径已补充 SLF4J 日志，便于排查配置更新失败原因。
6. 本批未迁移 `SpringMvcMigrationServlet`，因为它是路由适配入口，不属于普通 Spring Bean 注入字段改造范围；内部页面 DTO 的 `m_` 字段也暂不调整，避免影响 JSP/JSON 暴露属性。

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```
## 31. 第二十三批完成记录

第二十三批扩大到报表服务和定时报表构建链路，目标是把一组核心 `ReportService` 与对应 `TaskBuilder` 从 `CatHomeSpringConfiguration` 的显式 `@Bean` 注册迁移为 `@Component` 注册，同时保留原有任务名和初始化语义。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 `ReportService` 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
StateReportService
EventReportService
HeartbeatReportService
DependencyReportService
MatrixReportService
TransactionReportService
TopReportService
CrossReportService
ProblemReportService
StorageReportService
BusinessReportService
```

2. 以下 `TaskBuilder` 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描。原先显式指定 `@Bean(name = ID)` 的 builder 继续使用显式组件名；原先使用默认方法名的 `ProblemReportBuilder`、`StorageReportBuilder` 保留默认组件名，确保 `ReportFacade` 初始化时的 builder alias 计数和旧注册语义一致：

```text
StateReportBuilder
EventReportBuilder
HeartbeatReportBuilder
DependencyReportBuilder
MatrixReportBuilder
TransactionReportBuilder
CrossReportBuilder
ProblemReportBuilder
StorageReportBuilder
BusinessBaselineReportBuilder
```

3. 已删除 `CatHomeSpringConfiguration` 中对应的旧 `@Bean` 方法，避免组件扫描注册和配置类注册同时存在。

4. `ReportFacade` 会把 Spring beanName 和 builder 的 `ID` 都加入内部映射，因此保留默认组件名不会影响任务表按 `report_name` 查找 builder。

5. 旧配置方法中带 `initMethod = "initialize"` 的 builder 已改为在原 `initialize()` 方法上使用 `@PostConstruct`，保留启动初始化语义。

6. `AbstractReportService` 中的报表仓储依赖已改为 `@Resource` 字段注入，并保留 setter，方便测试或少量仍由配置类创建的相邻 Bean 继续复用：

```text
m_hourlyReportDao           -> hourlyReportRepository
m_hourlyReportContentDao    -> hourlyReportContentRepository
m_dailyReportDao            -> dailyReportRepository
m_dailyReportContentDao     -> dailyReportContentRepository
m_weeklyReportDao           -> weeklyReportRepository
m_weeklyReportContentDao    -> weeklyReportContentRepository
m_monthlyReportDao          -> monthlyReportRepository
m_monthlyReportContentDao   -> monthlyReportContentRepository
m_domains                   -> domainCache
```

7. 本批触碰到的 `TaskBuilder` 字段命名已收口为 Java 驼峰命名，并改为 `@Resource` 字段注入：

```text
m_reportService                 -> reportService
m_serverConfigManager           -> serverConfigManager
m_serverFilterConfigManager     -> serverFilterConfigManager
m_projectService                -> projectService
m_hostinfoService               -> hostinfoService
m_atomicMessageConfigManager    -> atomicMessageConfigManager
m_graphBuilder                  -> topologyGraphBuilder
m_topologyGraphDao              -> topologyGraphRepository
m_storageMergerHelper           -> storageMergeHelper
m_configManager                 -> businessConfigManager
m_baselineConfigManager         -> baselineConfigManager
m_parser                        -> businessPointParser
m_baselineCreator               -> baselineCreator
m_baselineService               -> baselineService
m_keyHelper                     -> businessKeyHelper
```

8. `AbstractReportService` 中原来只调用 `Cat.logError` 的插入、清理和查询异常路径已补充 SLF4J 日志，便于排查报表落库和查询问题。

9. 因 `AbstractReportService` 的 protected 字段改名，继承它的统计报表服务、路由报表服务、报表重载器等引用同步更新；容量统计相关类中同名 DAO 字段也做了驼峰命名清理。

10. 本批仍不迁移以下内容，避免同时改变聚合装配、模型服务和后台任务运行语义：

```text
ReportManager
ReportDelegate
ModelService / LocalModelService / RemoteModelService / CompositeModelService
ReportReloader 具体注册方法
Jar/Heavy/Client/Service/Utilization 统计报表 builder
CurrentReportBuilder、TaskConsumer、TaskManager、ReportFacade 等任务运行时 Bean
Map/List 聚合 Bean
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

## 32. 第二十四批完成记录

第二十四批继续沿着报表服务和定时报表构建链路推进，迁移 statistics 报表相关的 `ReportService` 与 `TaskBuilder`。这批与第二十三批模式一致，但范围限定在统计报表，避免同时改动容量统计、Router 和任务运行时 Bean。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 statistics `ReportService` 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
JarReportService
HeavyReportService
ClientReportService
ServiceReportService
UtilizationReportService
```

2. 以下 statistics `TaskBuilder` 已改为 `@Component(ID)` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描，保持旧 `@Bean(name = ID)` 的命名语义：

```text
JarReportBuilder
HeavyReportBuilder
ClientReportBuilder
ServiceReportBuilder
UtilizationReportBuilder
```

3. 已删除 `CatHomeSpringConfiguration` 中对应的旧 `@Bean` 方法，避免组件扫描注册和配置类注册同时存在。

4. 本批触碰到的 `TaskBuilder` 注入字段已改为 Java 驼峰命名，并使用 `@Resource` 字段注入：

```text
m_reportService              -> reportService
m_heartbeatReportService     -> heartbeatReportService
m_matrixReportService        -> matrixReportService
m_transactionReportService   -> transactionReportService
m_crossReportService         -> crossReportService
m_configManager              -> serverFilterConfigManager
m_configManger               -> serverFilterConfigManager
m_projectService             -> projectService
m_mergeHelper                -> transactionMergeHelper
```

5. 保留 builder setter 方法，便于现有测试或手工装配继续覆盖依赖。

6. `JarReportBuilder.HeartbeatReportVisitor` 内部短生命周期状态字段仍保留原命名，本批不扩大到内部 visitor 状态对象重命名，避免引入无关变化。

7. 本批仍不迁移以下内容：

```text
CapacityUpdateStatusManager
HourlyCapacityUpdater / DailyCapacityUpdater / WeeklyCapacityUpdater / MonthlyCapacityUpdater
CapacityUpdateTask
TableCapacityService
RouterConfigHandler / RouterConfigAdjustor / RouterConfigBuilder
ReportManager / ReportDelegate / ModelService
Map/List 聚合 Bean
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

## 33. 第二十五批完成记录

第二十五批迁移 overload/capacity 容量统计任务链路，承接前两批报表 `TaskBuilder` 迁移，继续缩减 `CatHomeSpringConfiguration` 中 report/task 区域的显式注册。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
CapacityUpdateStatusManager
HourlyCapacityUpdater
DailyCapacityUpdater
WeeklyCapacityUpdater
MonthlyCapacityUpdater
CapacityUpdateTask
TableCapacityService
```

2. 四个 `CapacityUpdater` 实现和 `CapacityUpdateTask` 保留旧显式 Bean 名称：

```text
HourlyCapacityUpdater.ID
DailyCapacityUpdater.ID
WeeklyCapacityUpdater.ID
MonthlyCapacityUpdater.ID
CapacityUpdateTask.ID
```

3. `CapacityUpdateStatusManager.initialize()` 已从配置类 `initMethod` 改为 `@PostConstruct`，保留启动时读取或初始化容量扫描状态的语义。

4. `CapacityUpdateTask` 中四个 `CapacityUpdater` 是同接口多实现，已使用显式名称注入，避免注入歧义：

```text
@Resource(name = HourlyCapacityUpdater.ID)
@Resource(name = DailyCapacityUpdater.ID)
@Resource(name = WeeklyCapacityUpdater.ID)
@Resource(name = MonthlyCapacityUpdater.ID)
```

5. 已删除 `CatHomeSpringConfiguration` 中对应的旧 `@Bean` 方法，避免组件扫描注册和配置类注册同时存在。

6. 本批触碰到的旧式字段命名已收口为 Java 驼峰命名，并使用 `@Resource` 字段注入：

```text
m_configDao         -> configRepository
m_overloadDao       -> overloadRepository
m_manager           -> capacityUpdateStatusManager
m_hourlyStatus      -> hourlyStatus
m_dailyStatus       -> dailyStatus
m_weeklyStatus      -> weeklyStatus
m_monthlyStatus     -> monthlyStatus
m_configId          -> configId
m_hourlyUpdater     -> hourlyCapacityUpdater
m_dailyUpdater      -> dailyCapacityUpdater
m_weeklyUpdater     -> weeklyCapacityUpdater
m_monthlyUpdater    -> monthlyCapacityUpdater
```

7. 本批仍不迁移以下内容：

```text
RouterConfigHandler / RouterConfigAdjustor / RouterConfigBuilder
TopologyGraphManager
ReportManager / ReportDelegate / ModelService
Map/List 聚合 Bean
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

## 34. 第二十六批完成记录

第二十六批迁移 router config 链路，承接上一批容量统计任务链路，继续缩减 `CatHomeSpringConfiguration` 中 report/task 相关显式 Bean 注册。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 router config 相关 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
RouterConfigService
CachedRouterConfigService
RouterConfigManager
RouterConfigHandler
RouterConfigAdjustor
RouterConfigBuilder
```

2. `RouterConfigBuilder` 保留旧 `@Bean(name = RouterConfigBuilder.ID)` 的命名语义，改为：

```java
@Component(RouterConfigBuilder.ID)
```

3. `CachedRouterConfigService.initialize()` 和 `RouterConfigManager.initialize()` 已从配置类 `initMethod` 改为 `@PostConstruct`，保留启动初始化和定时刷新注册语义。

4. 已删除 `CatHomeSpringConfiguration` 中对应旧 `@Bean` 方法，避免组件扫描注册和配置类注册同时存在：

```text
routerConfigService
cachedRouterConfigService
routerConfigHandler
routerConfigAdjustor
routerConfigBuilder
routerConfigManager
```

5. 本批触碰到的旧式字段命名已收口为 Java 驼峰命名，并使用 `@Resource` 字段注入：

```text
m_routerConfigManager    -> routerConfigManager
m_routerConfigService    -> routerConfigService
m_routerConfig           -> routerConfig
m_initialized            -> initialized
m_routerAdjustor         -> routerConfigAdjustor
m_reportService          -> reportService
m_serverConfigManager    -> serverConfigManager
m_stateReportService     -> stateReportService
m_configManager          -> routerConfigManager
m_routerService          -> routerConfigService
m_configDao              -> configRepository
m_fetcher                -> contentFetcher
m_configId               -> configId
m_modifyTime             -> modifyTime
m_subNetInfos            -> subNetInfos
m_ipToGroupInfo          -> ipToGroupInfo
m_routerConfigs          -> routerConfigs
```

6. `RouterConfigAdjustor.updateRouterConfigToDB()` 原来只调用 `Cat.logError`，本批补充了 SLF4J error 日志，方便排查 router config 写库失败。

7. 本批仍不迁移以下内容：

```text
TopologyGraphManager
ReportManager / ReportDelegate / ModelService
Map/List 聚合 Bean
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

## 35. 第二十七批完成记录

第二十七批迁移 dependency topology graph 链路，承接上一批文档中保留的 `TopologyGraphManager` 独立链路，继续缩减 `CatHomeSpringConfiguration` 中显式 Bean 注册。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
TopologyGraphManager
TopologyGraphConfigManager
TopoGraphFormatConfigManager
```

2. `TopologyGraphManager.initialize()`、`TopologyGraphConfigManager.initialize()`、`TopoGraphFormatConfigManager.initialize()` 已从配置类 `initMethod` 改为 `@PostConstruct`，保留启动初始化语义。

3. `TopologyGraphManager` 中 `ModelService<DependencyReport>` 是同类型多实现，已使用旧 Bean 名称显式注入，避免注入歧义：

```java
@Resource(name = "dependencyModelService")
```

4. 已删除 `CatHomeSpringConfiguration` 中对应旧 `@Bean` 方法，避免组件扫描注册和配置类注册同时存在：

```text
topologyGraphManager
topologyGraphConfigManager
topoGraphFormatConfigManager
```

5. 本批触碰到的旧式字段命名已收口为 Java 驼峰命名，并使用 `@Resource` 字段注入：

```text
m_service                   -> dependencyModelService
m_itemBuilder               -> dependencyItemBuilder
m_configManager             -> topoGraphFormatConfigManager
m_manager                   -> serverConfigManager
m_serverFilterConfigManager -> serverFilterConfigManager
m_projectService            -> projectService
m_topologyGraphDao          -> topologyGraphRepository
m_currentBuilder            -> currentBuilder
m_topologyGraphs            -> topologyGraphs
m_configDao                 -> configRepository
m_fetcher                   -> contentFetcher
m_config                    -> config
m_configId                  -> configId
m_df                        -> decimalFormat
m_fileName                  -> fileName
m_pigeonCalls               -> pigeonCalls
m_pigeonServices            -> pigeonServices
```

6. `TopologyGraphConfigManager` 和 `TopoGraphFormatConfigManager` 中原来只调用 `Cat.logError` 的初始化、插入和存储异常路径，已补充 SLF4J 日志，方便排查拓扑配置加载和保存问题。

7. 本批仍不迁移以下内容：

```text
ReportManager / ReportDelegate / ModelService
Map/List 聚合 Bean
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

## 36. 第二十八批完成记录

第二十八批迁移报表页面配置 Manager，继续收口 `ConfigRepository + ContentFetcher + initialize()` 形态的低风险配置类。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
DomainGroupConfigManager
StorageGroupConfigManager
HeartbeatDisplayPolicyManager
```

2. 三个 Manager 的 `initialize()` 已从配置类 `initMethod` 改为 `@PostConstruct`，保留启动加载配置语义。

3. 已删除 `CatHomeSpringConfiguration` 中对应旧 `@Bean` 方法，避免组件扫描注册和配置类注册同时存在：

```text
domainGroupConfigManager
storageGroupConfigManager
heartbeatDisplayPolicyManager
```

4. 本批触碰到的旧式字段命名已收口为 Java 驼峰命名，并使用 `@Resource` 字段注入：

```text
m_configDao       -> configRepository
m_fetcher         -> contentFetcher
m_configId        -> configId
m_domainGroup     -> domainGroup
m_config          -> config
m_id              -> id
m_productlines    -> productlines
m_storages        -> storages
```

5. `StorageGroupConfigManager` 和 `HeartbeatDisplayPolicyManager` 中原来只调用 `Cat.logError` 的初始化、插入、存储异常路径，已补充 SLF4J 日志；`DomainGroupConfigManager` 保留并适配已有 SLF4J 日志。

6. 本批仍不迁移以下内容：

```text
ReportManager / ReportDelegate / ModelService
Map/List 聚合 Bean
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

## 37. 第二十九批完成记录
第二十九批扩大到基础配置 Manager、登录权限服务、项目主机服务和任务层轻量 Bean，目标是继续减少 `CatHomeSpringConfiguration` 中显式 `@Bean` 注册数量，同时保持原有初始化时机和 Bean 命名语义。

状态：已完成，完成时间 2026-06-27。

完成内容：
1. 以下 Bean 已改为 `@Component` 创建，并加入 `CatHomeSpringConfiguration` 白名单扫描：

```text
AllReportConfigManager
ServerFilterConfigManager
SampleConfigManager
ReportReloadConfigManager
AtomicMessageConfigManager
TpValueStatisticConfigManager
UserConfigManager
ResourceConfigManager
CookieManager
TokenBuilder
DefaultCatPropertyProvider
TokenManager
SessionManager
SigninService
ProjectService
HostinfoService
TaskManager
DefaultTaskConsumer
ReportFacade
CurrentReportBuilder
ProjectUpdateTask
CmdbInfoReloadBuilder
ReportReloadTask
```

2. 原先带 `initMethod = "initialize"` 的类已改为 `@PostConstruct`，保留启动初始化语义；`HostinfoService` 原先没有 `initMethod`，本批继续保持懒初始化，避免组件化后提前启动刷新线程。
3. `CurrentReportBuilder` 和 `CmdbInfoReloadBuilder` 使用 `@Component(ID)` 保留旧 `@Bean(name = ID)` 的任务名语义。
4. `ReportReloadTask` 使用 `@Resource(name = "reportReloaders")` 注入既有聚合 Map；`ReportFacade` 使用 `@Resource(name = "taskBuilders")` 注入新增的 `taskBuilders` 聚合 Map，并保留原 `TaskBuilder` 别名构建逻辑。
5. 已删除 `CatHomeSpringConfiguration` 中对应 23 个旧 `@Bean` 方法，避免组件扫描注册和配置类注册同时存在。
6. 本批触碰到的 Spring 注入字段已按 Java 驼峰命名收口，并使用 `@Resource` 字段注入；原 setter 保留给测试和少量手工构造场景使用。
7. 对本批触碰且原先只调用 `Cat.logError` 的关键异常路径补充了 SLF4J 日志，覆盖配置加载/保存、任务创建、CMDB 更新等排查入口。
8. 本批仍不迁移以下内容：

```text
ServerConfigManager
BusinessConfigManager
ReportManager / ReportDelegate / ModelService
存储 bucket / HDFS / message dump 链路
DataSource / SqlSessionFactory / TransactionTemplate
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```

## 40. 第三十二批完成记录

第三十二批开始收口前面暂留的 List/Map 聚合 Bean，重点处理 storage、problem handler 和告警发送链路。目标是在不改变业务 key 语义的前提下，把聚合逻辑从 `CatHomeSpringConfiguration` 移到实际使用方内部，让配置类继续瘦身。

状态：已完成，完成时间 2026-06-27。

完成内容：

1. 以下聚合 Bean 已从 `CatHomeSpringConfiguration` 删除：

```text
problemHandlers
storageBuilders
storageBuilderManager
alertSenders
alertSpliters
alertContactors
alertDecorators
```

2. `StorageBuilderManager` 已改为 `@Component` 注册，并加入 `CatHomeSpringConfiguration` 白名单扫描；原 `@Bean(initMethod = "initialize")` 的初始化语义改为 `@PostConstruct`。
3. `ProblemAnalyzer` 改为直接注入 `List<ProblemHandler>`，不再依赖 `problemHandlers` 聚合 Bean。
4. `StorageAnalyzer` 和 `StorageBuilderManager` 改为注入 `List<StorageBuilder>`，初始化时按 `StorageBuilder#getType()` 构建业务 Map，保持旧配置类中按 type 作为 key 的语义。
5. `SenderManager`、`SpliterManager`、`ContactorManager`、`DecoratorManager` 改为注入对应组件列表，并分别按业务 ID 构建 Map：

```text
Sender#getId()
Spliter#getID()
Contactor#getId()
Decorator#getId()
```

6. 本批保留各 Manager 原有 setter，方便测试和少量手工构造场景继续覆盖聚合 Map。
7. 聚合构建过程补充了关键日志：当组件列表为空、业务 ID 为空或业务 ID 重复时，使用 SLF4J 输出清晰日志，便于启动和运行时排查组件扫描/注入问题。
8. 本批仍不迁移以下内容：

```text
ReportManager / ModelService / LocalModelService
reportReloaders / taskBuilders 等任务入口聚合 Map
存储 bucket / HDFS / message dump 链路
DataSource / SqlSessionFactory / TransactionTemplate
```

验证记录：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

结果：

```text
BUILD SUCCESS
git diff --check 通过
```
