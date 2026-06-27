# CAT Bean 组件化迁移准备与约束

## 1. 阶段目标

本阶段只做准备和约束，不迁移具体 Bean，不删除 `CatHomeSpringConfiguration` 中的 `@Bean` 定义。

目标是先固定后续批量迁移的规则，避免把普通业务 Bean、基础设施 Bean、聚合 Bean、生命周期 Bean 和运行时任务混在一起改，导致重复初始化、注入歧义或启动问题难以排查。

## 2. 当前基线

统计时间：2026-06-27。

当前集中注册入口：

```text
cat-home/src/main/java/com/dianping/cat/home/spring/CatHomeSpringConfiguration.java
```

当前统计：

```text
@Bean 总数：377
initMethod：116
destroyMethod：1
@Scope("prototype")：11
```

当前工作区已有未提交改动，后续迁移时不要回滚无关修改：

```text
cat-home/src/main/java/com/dianping/cat/home/spring/web/SpringMvcStateController.java
```

## 3. 迁移边界

后续迁移的目标是把适合组件扫描的业务类从 `@Bean` 方法迁移为类上的 Spring 注解：

```java
@Component
public class XxxManager {
   @Resource
   private YyyRepository yyyRepository;
}
```

依赖注入约束：

1. 本轮迁移统一使用 `@Resource` 注入成员变量。
2. 多实现、多同类型 Bean 必须显式指定名称，例如 `@Resource(name = "transactionReportManager")`。
3. 原来 `@Bean(name = "...")` 有显式名称的，迁移后必须保留同名 Bean。
4. 不为了迁移而新增 setter；测试确实需要注入时，优先使用 Lombok 的 `@Setter`，并控制可见性和范围。
5. 不批量新增 `@PostConstruct`。只有原来 `@Bean(initMethod = "...")` 已经声明生命周期方法时，才按原语义迁移。

## 4. Bean 分类策略

### 4.1 优先迁移

这些 Bean 通常适合迁移为 `@Component`：

1. 普通 Manager、Service、Helper、Builder。
2. 已经没有旧 Lookup 容器强依赖的 Analyzer、ReportBuilder。
3. 只通过成员依赖完成工作的业务类。
4. 当前已经在 Spring 中使用且不存在同类型歧义的类。

迁移时要同步处理：

1. 类上增加 `@Component`，必要时指定 Bean 名称。
2. 成员变量改为标准 Java 驼峰命名。
3. 触碰到的非标准变量名同步重命名为标准 Java 驼峰命名。
4. 成员变量使用 `@Resource` 注入。
5. 删除 `CatHomeSpringConfiguration` 中对应 `@Bean` 方法。
6. 如果类中有异常吞掉、只 `Cat.logError(e)`、或缺少上下文日志，顺手补充 SLF4J 日志。

### 4.2 谨慎迁移

这些 Bean 可以迁移，但必须小批量做，并在每批后运行编译和页面验证：

1. 有 `initMethod = "initialize"` 的 Manager、ModelService、ReportService。
2. Analyzer 及其依赖链。
3. Task、Consumer、Receiver、Reloader、Updater 等后台运行时组件。
4. ReportManager，尤其是当前 `@Scope("prototype")` 的 11 个报告管理器。
5. MVC Handler、Payload、Model、JSP Viewer 相关类。

谨慎点：

1. 原来是 prototype 的 Bean 不能直接改成默认 singleton。
2. 原来有 `initialize()` 的类，迁移后要确认初始化顺序没有变化。
3. 后台线程类要确认不会因为双注册导致重复消费、重复 heartbeat、重复 checkpoint。
4. Web 页面相关类要确认新旧 `/cat/r/...` 和 `/cat/mvc/r/...` 路径不会互相污染。

### 4.3 暂不迁移

这些 Bean 暂时保留在 `CatHomeSpringConfiguration` 中：

1. 基础设施 Bean：`DataSource`、`SqlSessionFactory`、`SqlSessionTemplate`、事务管理器、事务模板。
2. 聚合 Bean：`Map`、`List`、`Set` 类型的集合装配，例如 reloaders、handlers、builders、senders。
3. 第三方库或工厂方法创建的 Bean。
4. 匿名内部类、lambda、需要复杂构造参数的适配 Bean。
5. 存储底层设施和 legacy bucket/dumper/index manager，除非已经单独验证生命周期。
6. 只为兼容旧 Unidal/Plexus 桥接链路存在的 Bean。

## 5. 当前大类盘点

`CatHomeSpringConfiguration` 中的 Bean 约可分为：

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

第一轮建议从低风险类开始，例如：

1. 已经迁移过一部分的 Analyzer，继续保持同一风格收口。
2. 无复杂生命周期的 Helper、Builder。
3. 简单 Manager 或 Service。

暂时不要从基础设施、聚合集合、prototype report manager、后台 runtime 大类开始。

## 6. 生命周期规则

迁移生命周期时只保留已有语义，不新增无意义生命周期钩子。

映射规则：

```text
@Bean(initMethod = "initialize")  -> 迁移已有 initialize() 的初始化语义
@Bean(initMethod = "start")       -> 迁移已有 start() 的启动语义
@Bean(destroyMethod = "shutdown") -> 使用 @PreDestroy 或等价关闭语义
@Bean(initMethod = "verify")      -> 保留 verify() 启动校验语义
```

约束：

1. 不因为“想看日志”就在所有类里加 `@PostConstruct`。
2. 只有原本在配置类中声明过 `initMethod` 的 Bean，才需要讨论初始化注解。
3. 初始化失败必须让 Spring 启动失败，不能吞掉异常后继续运行。
4. 如果初始化依赖外部配置，日志里要包含 Bean 名称、配置名、关键参数和异常堆栈。

## 7. 日志规则

迁移过程中只在被触碰的类里顺手补日志，不做全仓库机械替换。

需要补强的情况：

1. `catch` 后没有日志。
2. 只调用 `Cat.logError(e)`，日志里缺少类名、业务上下文或关键参数。
3. 捕获异常后返回默认值，但没有说明默认值来源。
4. 初始化、加载配置、启动线程、注册 handler、读写数据库失败时没有 SLF4J 日志。

需要重点观察的关键代码位置：

1. Bean 初始化入口：`initialize()`、`start()`、`verify()`、`shutdown()`。
2. 配置加载入口：server config、router config、alarm config、business config、rule config 等配置读取和解析位置。
3. 后台任务入口：task producer、task consumer、report builder、updater、reloader、checkpoint。
4. 消息链路入口：receiver、decoder、processor、dumper、bucket manager。
5. 页面核心查询入口：model service、report service、history/day/hour report 查询。
6. 聚合注册入口：handler、builder、sender、spliter、reloader、model service 的 Map/List 装配和选择逻辑。
7. 异常降级入口：查询不到数据、配置不存在、任务为空、外部存储不可用后返回默认值的位置。

关键位置建议补充系统状态日志：

1. 启动类组件时记录 Bean 名称、关键开关、domain、report type、线程名等信息。
2. 注册类组件时记录注册对象数量、注册 key、是否覆盖已有对象。
3. 定时或后台任务开始和结束时记录任务类型、时间窗口、domain、消费机器、处理数量和耗时。
4. 读写报告或存储文件时记录 report name、period、domain、ip、路径、数据大小和耗时。
5. 捕获异常后返回默认值时记录默认值来源和影响范围。
6. 可能导致重复处理的位置记录唯一标识，例如 consumer ip、task id、period、线程名。

推荐写法：

```java
private static final Logger logger = LoggerFactory.getLogger(Xxx.class);

try {
   ...
} catch (Exception e) {
   logger.error("Unable to load xxx config, name={}, domain={}.", name, domain, e);
   Cat.logError(e);
}
```

约束：

1. 保留必要的 `Cat.logError(e)`，避免影响 CAT 自身错误上报。
2. 同时补充 SLF4J，使启动日志和应用日志中可以直接看到上下文。
3. 不记录敏感信息。
4. 不把可恢复的业务缺省值全部打成 error；按影响使用 warn 或 error。
5. 不在高频循环里打印无条件 info 日志；高频路径只打印开始/结束摘要、异常、状态变化，必要时使用 debug。
6. 日志要能回答“哪个 Bean、哪个 domain、哪个 period、哪个任务、哪台机器、失败原因是什么”。

## 8. 命名规则

类成员变量：

```text
m_reportManager    -> reportManager 或具体类型名，例如 transactionReportManager
m_configManager    -> configManager 或具体类型名，例如 serverFilterConfigManager
m_statisticManager -> tpValueStatisticConfigManager
```

变量重命名规则：

1. 迁移某个 Bean 时，当前类中被触碰到的成员变量必须同步改成标准 Java 驼峰命名。
2. 明显不规范的局部变量、方法参数，如果位于本次修改范围内，也顺手改成标准 Java 驼峰命名。
3. 旧式 `m_` 前缀、下划线命名、缩写堆叠命名，应改成表达具体含义的驼峰命名。
4. 同类型依赖较多时，不使用过于泛化的名称，例如优先使用 `transactionReportManager`，而不是 `reportManager`。
5. 不为了命名而跨模块做大范围机械重命名；只处理当前迁移 Bean 及其直接相关测试。
6. 常量、枚举值、数据库字段名、配置 key、JSP 参数名、URL 参数名、序列化字段名不按本规则强行重命名，避免破坏外部协议。
7. 如果变量名属于 public/protected API 或被 JSP、反射、配置、序列化依赖，必须先确认引用方，再决定是否重命名。

Bean 名称：

1. 原来使用常量命名的 Bean，迁移后继续使用同一个常量。
2. 原来有字符串名称的 Bean，迁移后继续使用同一个字符串。
3. 多个同类型实现必须显式命名，避免 `@Resource` 按类型误注入。
4. 不在同一次提交里同时做大范围命名和行为重构。

## 9. 验证规则

每一小批迁移完成后至少执行：

```powershell
mvn -pl cat-home -am -DskipTests compile
```

涉及启动、后台任务、Bean 生命周期时，增加：

```powershell
mvn -pl cat-boot -am package -DskipTests "-Dmaven.javadoc.skip=true"
```

涉及页面时，至少访问对应新旧页面进行对比：

```text
http://localhost:8080/cat/r/...
http://localhost:8080/cat/mvc/r/...
```

涉及运行时任务、消息消费、checkpoint、heartbeat、task consumer 时，必须额外观察：

```text
是否重复启动线程
是否重复注册 handler
是否 heartbeat 变成每分钟 2 次
是否访问页面导致自身 transaction total 异常增加
是否重启后当前小时数据丢失
是否 task consumer 空结果被当成 error
```

## 10. 执行节奏

建议每批只迁移一个小类群：

1. 先查 `CatHomeSpringConfiguration` 中对应 `@Bean` 方法和依赖链。
2. 再看类本身是否已有旧 Lookup 注解、setter、initialize、日志处理。
3. 改类注解、字段注入和命名。
4. 删除配置类中的对应 `@Bean` 方法。
5. 编译。
6. 根据影响范围做页面或运行时验证。
7. Review 未提交改动，确认没有双注册和无关格式化。
8. Review 命名变更，确认变量名已经符合标准 Java 驼峰，且没有破坏外部协议或反射/JSP 引用。

本阶段完成后，后续迁移应从小批量开始，不一次性处理所有 377 个 Bean。
