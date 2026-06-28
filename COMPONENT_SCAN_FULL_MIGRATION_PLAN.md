# CAT Home 全量 ComponentScan 迁移计划

## 目标

最终让 `CatHomeSpringConfiguration` 只保留一个默认包扫描入口：

```java
@Configuration
@ComponentScan("com.dianping.cat")
public class CatHomeSpringConfiguration {
}
```

最终状态要求：

1. `CatHomeSpringConfiguration` 不再配置 `includeFilters`。
2. `CatHomeSpringConfiguration` 不再配置 `useDefaultFilters = false`。
3. `CatHomeSpringConfiguration` 不再依赖大段 `ASSIGNABLE_TYPE` 白名单。
4. `CatHomeSpringConfiguration` 不再通过 `@Import` 手工导入会被 `com.dianping.cat` 默认扫描发现的配置类。
5. 入口配置类只配置 `ComponentScan=com.dianping.cat`，后续不能再把局部扫描白名单加回入口配置类。
6. 所有进入 Spring 容器的业务 Bean 通过 `@Component`、`@Controller`、`@Configuration` 或 MyBatis `@MapperScan` 这类标准机制注册。

## 当前状态

当前 `CatHomeSpringConfiguration` 已经进入最终形态：

```java
@Configuration
@ComponentScan("com.dianping.cat")
public class CatHomeSpringConfiguration {
}
```

当前仍保留的非组件直接注册：

```text
@Bean: 5 个，集中在 CatHomeDatabaseConfiguration
@MapperScan: 负责 MyBatis Mapper 代理注册
@Configuration: 作为配置入口或子配置存在
```

这 5 个 `@Bean` 是数据库基础设施 Bean，暂时保留：

```text
catDataSource
sqlSessionFactory
sqlSessionTemplate
transactionManager
transactionTemplate
```

## 风险边界

去掉 `includeFilters` 后，默认扫描会启用所有 stereotype 注解：

```text
@Component
@Controller
@Configuration
@Service
@Repository
```

主要风险：

1. `@Configuration` 类被扫描后，如果入口仍 `@Import` 同一批配置类，可能重复处理配置。
2. `com.dianping.cat.home.spring.web` 下的 `@Controller` 会进入 root context。当前迁移 servlet 正是从 root context 获取这些 Controller，因此这是预期行为，但必须避免重复扫描。
3. storage 包下大量带 `@PostConstruct` 的组件会通过默认扫描进入 root context。当前入口已经全量扫描 `com.dianping.cat`，不再依赖迁移期的局部 storage 扫描配置。
4. 旧 MVC `Handler`、`JspViewer` 也会被默认扫描。它们原先已经通过白名单注册，因此预期行为保持一致。
5. 裸 `@Resource` 在 Bean 变多后可能出现按类型回退导致的歧义，需要通过启动验证发现并逐个改成显式 `@Resource(name = "...")`。
6. 完整 Spring context 会触发 `CatHomeRuntimeBootstrap`，它会启动 socket receiver 和后台线程，运行验证必须确保能正常关闭上下文。

## 执行记录

### 步骤 1：建立迁移文档

已新增本文档，明确最终目标、风险边界、验证方式和回退点。

验证结果：

```text
mvn -pl cat-home -am -DskipTests compile: 通过
git diff --check: 通过
```

### 步骤 2A：让子配置类先进入当前白名单扫描

已在保留 `includeFilters` 的阶段，把以下配置类加入当时的 `ASSIGNABLE_TYPE` 白名单：

```text
CatHomeDatabaseConfiguration
SpringMvcMigrationConfiguration
SpringStorageComponentConfiguration
```

这两个局部扫描配置类只服务于迁移过程，最终形态已经删除。

验证结果：

```text
mvn -pl cat-home -am -DskipTests compile: 通过
git diff --check: 通过
```

### 步骤 2B：移除入口配置中的重复 `@Import`

已从 `CatHomeSpringConfiguration` 删除：

```text
@Import({
    CatHomeDatabaseConfiguration.class,
    SpringMvcMigrationConfiguration.class,
    SpringStorageComponentConfiguration.class
})
```

验证结果：

```text
mvn -pl cat-home -am -DskipTests compile: 通过
git diff --check: 通过
```

### 步骤 3：移除 `includeFilters` 和 `useDefaultFilters = false`

已将 `CatHomeSpringConfiguration` 改为最终形态：

```java
@Configuration
@ComponentScan("com.dianping.cat")
public class CatHomeSpringConfiguration {
}
```

验证结果：

```text
mvn -pl cat-home -am -DskipTests compile: 通过
git diff --check: 通过
```

### 步骤 4：删除迁移期局部扫描配置

已删除迁移期使用的空配置类：

```text
SpringMvcMigrationConfiguration
SpringStorageComponentConfiguration
```

删除后生产代码中只剩一个 `@ComponentScan`：

```text
cat-home/src/main/java/com/dianping/cat/home/spring/CatHomeSpringConfiguration.java:
@ComponentScan("com.dianping.cat")
```

验证结果：

```text
mvn -pl cat-home -am -DskipTests compile: 通过
mvn -pl cat-home -Dtest=SpringMvcMigrationConfigurationTest test: 通过
mvn -pl cat-home -Dtest=CatHomeSpringConfigurationTest test: 通过
git diff --check: 通过
```

## 后续验证

编译通过只能证明类型和依赖声明成立，还不能完全证明运行期 Spring context 没有注入歧义或重复初始化。

已补充受控的 Spring context 验证：

1. `CatHomeSpringConfigurationTest` 使用 `CatHomeSpringConfiguration` 刷新 root context 的 BeanDefinition。
2. 测试中禁止 BeanDefinition 覆盖，用于暴露重复 Bean 名问题。
3. 测试中把 BeanDefinition 统一设为 lazy，避免验证阶段启动 socket receiver、runtime bootstrap 和后台任务。
4. 测试断言迁移期局部扫描配置 Bean 已不存在，并确认关键 BeanDefinition 可被全量扫描发现。

仍建议在本地应用启动时再做一次真实启动观察：

1. 确认 `cat.home` 下存在可用的 `datasources.xml`。
2. 刷新 `CatHomeSpringConfiguration` 对应的 root context。
3. 验证 `CatHomeSpringContextListener` 日志中的 `beanCount`。
4. 观察以下组件是否只初始化或启动一次：

```text
CatHomeRuntimeBootstrap
RealtimeConsumer
ReportReloadTask
TaskConsumer
AlertManager / AlarmManager
storage manager
MyBatis mapper
```

5. 验证完成后关闭 Spring context，确认后台线程和 shutdown hook 被清理。

如果启动失败，按异常类型处理：

1. `NoUniqueBeanDefinitionException`：把对应字段改为 `@Resource(name = "...")`。
2. `BeanDefinitionOverrideException` 或同名 Bean 冲突：确认是否有重复 `@Component("name")` 或重复配置类扫描。
3. 初始化顺序问题：把依赖改为显式名称，必要时使用 `@DependsOn`，但优先保持现有初始化语义。
4. 重复线程或重复任务：确认是否仍存在重复扫描或重复 `@Import`。

每修复一个问题都执行：

```powershell
mvn -pl cat-home -am -DskipTests compile
git diff --check
```

## 回退策略

如果默认扫描导致短时间内难以收敛的问题，可以临时回退到最近稳定形态；但回退只应作为排查手段，最终目标仍然是入口配置类只保留：

```java
@Configuration
@ComponentScan("com.dianping.cat")
public class CatHomeSpringConfiguration {
}
```

回退后继续按问题清单推进，不能把 `includeFilters` 作为长期方案。
