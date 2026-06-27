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
2. Spring 启动时没有同名 Bean 冲突。编译已通过，运行时启动待后续验证。
3. `businessKeyHelper`、`businessDataFetcher`、`customDataCalculator`、`businessPointParser`、`baselineConfigManager`、`baselineCreator`、`cachedBusinessReportService`、`businessReportGroupService` 都由组件扫描创建。已通过白名单 include filter 纳入扫描。
4. `CatHomeSpringConfiguration` 中不再保留这 8 个 Bean 的 `@Bean` 方法。已完成。
5. 触碰类中的旧式 `m_` 字段已改为标准 Java 驼峰命名。已完成。
6. 触碰到的异常处理位置有 SLF4J 上下文日志。已补充 `BusinessDataFetcher`、`CustomDataCalculator`、`BusinessPointParser` 的日志。
7. 不影响业务页面和告警链路的现有行为。代码层面未主动修改算法和业务语义，页面与运行时行为待后续本地验证。

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
