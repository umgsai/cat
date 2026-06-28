# Unidal Web Removal Plan

本文档是后续移除 Unidal Web 相关依赖的执行计划。后续所有 Web 迁移、路由补齐、JSP 改造和依赖删除操作都应以本文档为主线推进。

## 1. 当前结论

当前系统已经存在两套 Web 入口：

1. `/mvc/*`
   - 由 `SpringMvcMigrationServlet` 处理。
   - 通过手工白名单路由调用 `SpringMvc*Controller`。
   - 已迁移页面不进入旧 `ReportModule`、旧 `SystemModule`、旧 `PageHandler`、旧 `SpringMvcRuntime`。

2. `/r/*` 和 `/s/*`
   - 由 `SpringMvcServlet` 处理。
   - 虽然 servlet 名称是 Spring MVC，但内部 `SpringMvcRuntime` 仍复用了 `org.unidal.web.mvc.*` 的 Module、PageHandler、Payload、ViewModel、JspViewer 模型。
   - 这是当前不能删除 `org.unidal.framework:web-framework` 的核心原因。

已确认示例 URL：

```text
/cat/mvc/r/t?domain=cat&ip=All&date=2026062600&reportType=day&op=view
```

执行链路为：

```text
web.xml /mvc/* -> SpringMvcMigrationServlet -> SpringMvcTransactionController -> jsp/spring/report/transaction/transaction.jsp
```

该请求主链路不依赖 Unidal MVC，且 `transaction.jsp` 及其 include 的 `navbar.jsp`、`reportSidebar.jsp` 只使用 JSTL，没有使用 Unidal JSP taglib。

## 2. 目标状态

最终状态必须满足：

1. 所有 Web 页面和接口都由 Spring Controller 或明确的 Servlet/Filter 处理。
2. 不再需要 `SpringMvcRuntime`、`ReportModule`、`SystemModule`、旧 `PageHandler`、旧 `Payload`、旧 `ViewModel`、旧 `JspViewer`。
3. JSP 不再引用 Unidal taglib：
   - `/WEB-INF/app.tld`
   - `/WEB-INF/web-core.tld`
   - `/WEB-INF/webres.tld`
4. `cat-home` 不再依赖：
   - `org.unidal.framework:web-framework`
   - `org.unidal.webres:WebResServer`
5. 根 POM 中可以删除 Web 层不再需要的 Unidal dependency management。
6. `/r/*`、`/s/*` 的最终对外路径保持可用，或者通过明确兼容跳转切换到新路径。

## 3. 路由覆盖现状

### 3.1 已有 `/mvc` 新链路覆盖

以下旧页面已经有 `/mvc` 新链路：

```text
/r/home
/r/p
/r/t
/r/e
/r/h
/r/m/*
/r/cross
/r/state
/r/top
/r/overload
/r/matrix
/r/model
/r/cache
/r/statistics
/r/business
/s/login
/s/config
/s/plugin
/s/router
/s/project
/s/business
/s/permission
```

### 3.2 缺少 `/mvc` 新链路的已注册旧页面

以下页面在 `ReportModule` 或 `SystemModule` 中注册，但 `SpringMvcMigrationServlet` 尚未覆盖：

```text
/r/dependency
/r/alteration
/r/alert
```

这些页面必须先完成 Spring 化，否则无法删除旧 Unidal MVC runtime。

### 3.2.1 下一批待迁移页面详情

以下页面是下一批建议优先迁移的缺口。它们都已经在 `ReportModule` 中注册，但当前
`SpringMvcMigrationServlet` 没有 `/mvc` 路由，访问旧路径仍会进入 Unidal MVC runtime。

#### `/r/model`

状态：已完成 `/mvc/r/model/*` 新链路。

路由状态：

```text
旧页面/API: /cat/r/model/{report}/{domain}/{period}?op=xml
目标新链路: /cat/mvc/r/model/{report}/{domain}/{period}?op=xml
当前状态: SpringMvcMigrationServlet 已注册 /r/model 和 /r/model/*，SpringMvcModelController 已覆盖
```

旧实现入口：

```text
模块注册: cat-home/src/main/java/com/dianping/cat/report/ReportModule.java
旧 Handler: cat-home/src/main/java/com/dianping/cat/report/page/model/Handler.java
旧 Payload: cat-home/src/main/java/com/dianping/cat/report/page/model/Payload.java
旧 Action: cat-home/src/main/java/com/dianping/cat/report/page/model/Action.java
旧 JSP: cat-home/src/main/webapp/jsp/report/model.jsp
```

迁移结果：

1. 该入口本质是 XML/GZIP 接口，不是普通 HTML 页面。
2. 只定义了 `op=xml`，默认也是 `xml`。
3. 路径参数来自新 controller 解析，语义保持为 `{report}/{domain}/{period}`。
4. `report=logview` 时使用 `messageId` 推导时间，其他 report 使用 `period.getStartTime()`。
5. 已复用 `LocalModelService` 服务表，覆盖 `problem/event/transaction/heartbeat/cross/matrix/dependency/top/state/storage/business/logview`。
6. 新 controller 直接写 `application/xml;charset=utf-8`，并保留 `Content-Encoding: gzip` 行为。
7. 已增加 controller 单测，覆盖 service 调用、响应头和 gzip 解压后的 XML 内容。

验收 URL 示例：

```text
旧: http://localhost:8080/cat/r/model/transaction/cat/current?op=xml
新: http://localhost:8080/cat/mvc/r/model/transaction/cat/current?op=xml
```

#### `/r/matrix`

状态：已完成 `/mvc/r/matrix` 新链路。

路由状态：

```text
旧页面: /cat/r/matrix?domain={domain}&ip=All&date={yyyyMMddHH}&reportType=day&op=view
目标新链路: /cat/mvc/r/matrix?domain={domain}&ip=All&date={yyyyMMddHH}&reportType=day&op=view
当前状态: SpringMvcMigrationServlet 已注册 /r/matrix，SpringMvcMatrixController 已覆盖
```

旧实现入口：

```text
模块注册: cat-home/src/main/java/com/dianping/cat/report/ReportModule.java
旧 Handler: cat-home/src/main/java/com/dianping/cat/report/page/matrix/Handler.java
旧 Payload: cat-home/src/main/java/com/dianping/cat/report/page/matrix/Payload.java
旧 Action: cat-home/src/main/java/com/dianping/cat/report/page/matrix/Action.java
旧 JSP: cat-home/src/main/webapp/jsp/report/matrix/matrix.jsp
旧历史 JSP: cat-home/src/main/webapp/jsp/report/matrix/matrixHistoryReport.jsp
```

迁移结果：

1. 已覆盖 `op=view` 和 `op=history`。
2. `op=view` 走小时报表链路，依赖 `matrixModelService` 查询 `MatrixReport`。
3. `op=history` 走汇总链路，依赖 `MatrixReportService#queryReport(domain, start, end)`。
4. 已迁移 `sort` 参数，并继续通过 `new DisplayMatrix(report).setSortBy(sort)` 构造展示模型。
5. 已新建 `jsp/spring/report/matrix/*`，不再使用 `/WEB-INF/app.tld`、`web-core`、`webres`。
6. 页面内链接、导航和资源引用已统一改成 `${contextPath}/mvc/...` 和普通静态资源引用。

验收 URL 示例：

```text
旧: http://localhost:8080/cat/r/matrix?domain=cat&ip=All&date=2026062720&reportType=day&op=view
新: http://localhost:8080/cat/mvc/r/matrix?domain=cat&ip=All&date=2026062720&reportType=day&op=view
旧: http://localhost:8080/cat/r/matrix?domain=cat&ip=All&date=2026062700&reportType=day&op=history
新: http://localhost:8080/cat/mvc/r/matrix?domain=cat&ip=All&date=2026062700&reportType=day&op=history
```

#### `/r/cache`

状态：已完成 `/mvc/r/cache` 新链路。

路由状态：

```text
旧页面: /cat/r/cache?domain={domain}&ip=All&date={yyyyMMddHH}&reportType=day&op=view
目标新链路: /cat/mvc/r/cache?domain={domain}&ip=All&date={yyyyMMddHH}&reportType=day&op=view
当前状态: SpringMvcMigrationServlet 已注册 /r/cache，SpringMvcCacheController 已覆盖
```

旧实现入口：

```text
模块注册: cat-home/src/main/java/com/dianping/cat/report/ReportModule.java
旧 Handler: cat-home/src/main/java/com/dianping/cat/report/page/cache/Handler.java
旧 Payload: cat-home/src/main/java/com/dianping/cat/report/page/cache/Payload.java
旧 Action: cat-home/src/main/java/com/dianping/cat/report/page/cache/Action.java
旧小时 JSP: cat-home/src/main/webapp/jsp/report/cache/cache.jsp
旧历史 JSP: cat-home/src/main/webapp/jsp/report/cache/cacheHistory.jsp
```

迁移结果：

1. 已覆盖 `op=view` 和 `op=history`。
2. `op=view` 走小时报表链路，依赖 `transactionModelService` 和 `eventModelService`。
3. `op=history` 走汇总链路，依赖 `TransactionReportService#queryReport` 和 `EventReportService#queryReport`。
4. 已保留 `ip`、`type`、`queryname`、`sort` 参数，继续通过 `TransactionReportVistor` 构造 `CacheReport`。
5. 已新建 `jsp/spring/report/cache/*`，不再使用 `/WEB-INF/app.tld`、`web-core`、`webres`。
6. 页面内链接、导航和资源引用已统一改成 `${contextPath}/mvc/...` 和普通静态资源引用。

验收 URL 示例：

```text
旧: http://localhost:8080/cat/r/cache?domain=cat&ip=All&date=2026062720&reportType=day&op=view
新: http://localhost:8080/cat/mvc/r/cache?domain=cat&ip=All&date=2026062720&reportType=day&op=view
旧: http://localhost:8080/cat/r/cache?domain=cat&ip=All&date=2026062700&reportType=day&op=history
新: http://localhost:8080/cat/mvc/r/cache?domain=cat&ip=All&date=2026062700&reportType=day&op=history
```

#### `/r/statistics`

状态：已完成 `/mvc/r/statistics` 新链路。

路由状态：

```text
旧页面: /cat/r/statistics?domain={domain}&op=service
目标新链路: /cat/mvc/r/statistics?domain={domain}&op=service
当前状态: SpringMvcMigrationServlet 已注册 /r/statistics GET/POST，SpringMvcStatisticsController 已覆盖
```

旧实现入口：

```text
模块注册: cat-home/src/main/java/com/dianping/cat/report/ReportModule.java
旧 Handler: cat-home/src/main/java/com/dianping/cat/report/page/statistics/Handler.java
旧 Payload: cat-home/src/main/java/com/dianping/cat/report/page/statistics/Payload.java
旧 Action: cat-home/src/main/java/com/dianping/cat/report/page/statistics/Action.java
旧 JSP: cat-home/src/main/webapp/jsp/report/service/service.jsp
旧 JSP: cat-home/src/main/webapp/jsp/report/service/serviceHistory.jsp
旧 JSP: cat-home/src/main/webapp/jsp/report/heavy/heavy.jsp
旧 JSP: cat-home/src/main/webapp/jsp/report/heavy/heavyHistory.jsp
旧 JSP: cat-home/src/main/webapp/jsp/report/utilization/utilization.jsp
旧 JSP: cat-home/src/main/webapp/jsp/report/utilization/utilizationHistory.jsp
旧 JSP: cat-home/src/main/webapp/jsp/report/jar/jar.jsp
旧 JSP: cat-home/src/main/webapp/jsp/report/statistics/clientReport.jsp
旧 JSP: cat-home/src/main/webapp/jsp/report/summary/summary.jsp
```

迁移结果：

1. 已覆盖 `op=service/historyService/client/utilization/historyUtilization/jar/heavy/historyHeavy/summary`。
2. 新 controller 不再复用旧 `Payload`、`Model`、`Action`，避免新链路继续绑定 Unidal MVC 类型。
3. 已复用旧统计报表服务：`ServiceReportService`、`ClientReportService`、`UtilizationReportService`、`JarReportService`、`HeavyReportService`、`AlertSummaryExecutor`。
4. 已新建 `jsp/spring/report/statistics/*`，不再使用 `/WEB-INF/app.tld`、`web-core`、`webres`。
5. 页面内导航和表格链接已统一改成 `${contextPath}/mvc/...`。

验收 URL 示例：

```text
旧: http://localhost:8080/cat/r/statistics?domain=cat&op=service
新: http://localhost:8080/cat/mvc/r/statistics?domain=cat&op=service
旧: http://localhost:8080/cat/r/statistics?domain=cat&op=historyService&reportType=day
新: http://localhost:8080/cat/mvc/r/statistics?domain=cat&op=historyService&reportType=day
旧: http://localhost:8080/cat/r/statistics?domain=cat&op=client
新: http://localhost:8080/cat/mvc/r/statistics?domain=cat&op=client
旧: http://localhost:8080/cat/r/statistics?domain=cat&op=utilization
新: http://localhost:8080/cat/mvc/r/statistics?domain=cat&op=utilization
旧: http://localhost:8080/cat/r/statistics?domain=cat&op=jar
新: http://localhost:8080/cat/mvc/r/statistics?domain=cat&op=jar
旧: http://localhost:8080/cat/r/statistics?domain=cat&op=heavy
新: http://localhost:8080/cat/mvc/r/statistics?domain=cat&op=heavy
旧: http://localhost:8080/cat/r/statistics?domain=cat&op=summary
新: http://localhost:8080/cat/mvc/r/statistics?domain=cat&op=summary
```

#### `/r/storage`

状态：已完成 `/mvc/r/storage` 新链路。

路由状态：

```text
旧页面: /cat/r/storage?id={id}&domain={domain}&ip=All&type=SQL&date={yyyyMMddHH}&op=view
目标新链路: /cat/mvc/r/storage?id={id}&domain={domain}&ip=All&type=SQL&date={yyyyMMddHH}&op=view
当前状态: SpringMvcMigrationServlet 已注册 /r/storage，SpringMvcStorageController 已覆盖
```

旧实现入口：

```text
模块注册: cat-home/src/main/java/com/dianping/cat/report/ReportModule.java
旧 Handler: cat-home/src/main/java/com/dianping/cat/report/page/storage/Handler.java
旧 Payload: cat-home/src/main/java/com/dianping/cat/report/page/storage/Payload.java
旧 Action: cat-home/src/main/java/com/dianping/cat/report/page/storage/Action.java
旧小时 JSP: cat-home/src/main/webapp/jsp/report/storage/storage.jsp
旧历史 JSP: cat-home/src/main/webapp/jsp/report/storage/historyStorage.jsp
旧小时图 JSP: cat-home/src/main/webapp/jsp/report/storage/hourlyGraphs.jsp
旧监控大盘 JSP: cat-home/src/main/webapp/jsp/report/storage/dashboard.jsp
```

迁移结果：

1. 已覆盖 `op=view/history/hourlyGraph/dashboard`。
2. `op=view` 和 `op=hourlyGraph` 走小时报表链路，依赖 `storageModelService` 查询 `StorageReport`。
3. `op=history` 走汇总链路，依赖 `StorageReportService#queryReport(id + "-" + type, start, end)`。
4. `op=dashboard` 复用 `AlertService`、`StorageAlertInfoBuilder`、`StorageGroupConfigManager` 和 `AlterationRepository`。
5. 已保留 `id`、`domain`、`ip`、`type`、`operations`、`sort`、`project`、`minute`、`count` 等参数。
6. 已新建 `jsp/spring/report/storage/*`，不再使用 `/WEB-INF/app.tld`、`web-core`、`webres`。
7. 页面内链接和异步小时图请求已统一改成 `${contextPath}/mvc/...`。

验收 URL 示例：

```text
旧: http://localhost:8080/cat/r/storage?id=cat&domain=cat&ip=All&type=SQL&date=2026062720&op=view
新: http://localhost:8080/cat/mvc/r/storage?id=cat&domain=cat&ip=All&type=SQL&date=2026062720&op=view
旧: http://localhost:8080/cat/r/storage?id=cat&domain=cat&ip=All&type=SQL&date=2026062700&reportType=day&op=history
新: http://localhost:8080/cat/mvc/r/storage?id=cat&domain=cat&ip=All&type=SQL&date=2026062700&reportType=day&op=history
旧: http://localhost:8080/cat/r/storage?domain=cat&type=SQL&date=2026062720&op=dashboard
新: http://localhost:8080/cat/mvc/r/storage?domain=cat&type=SQL&date=2026062720&op=dashboard
```

#### `/r/monitor`

状态：已完成 `/mvc/r/monitor` 新链路。

路由状态：

```text
旧接口: /cat/r/monitor?op=count&timestamp={timestamp}&group={group}&domain={domain}&key={key}&value={value}
目标新链路: /cat/mvc/r/monitor?op=count&timestamp={timestamp}&group={group}&domain={domain}&key={key}&value={value}
当前状态: SpringMvcMigrationServlet 已注册 /r/monitor GET/POST，SpringMvcMonitorController 已覆盖
```

旧实现入口：

```text
模块注册: cat-home/src/main/java/com/dianping/cat/report/ReportModule.java
旧 Handler: cat-home/src/main/java/com/dianping/cat/report/page/monitor/Handler.java
旧 Payload: cat-home/src/main/java/com/dianping/cat/report/page/monitor/Payload.java
旧 Action: cat-home/src/main/java/com/dianping/cat/report/page/monitor/Action.java
旧 JSP: cat-home/src/main/webapp/jsp/report/monitor.jsp
```

迁移结果：

1. 已覆盖 `op=count/avg/sum/batch`。
2. 旧 Handler 当前没有业务写入逻辑，旧 JSP 只输出 `model.status`，实际响应为 `200 text/html;charset=UTF-8` 空 body。
3. 新 controller 保留旧行为，直接返回 `text/html;charset=UTF-8` 空响应，不再依赖 Unidal `Payload/Model/Action/JspViewer`。
4. 已同时注册 GET/POST，保留外部调用兼容性。

验收 URL 示例：

```text
旧: http://localhost:8080/cat/r/monitor?op=count&timestamp=2026062720&group=test&domain=cat&key=myKey&value=1
新: http://localhost:8080/cat/mvc/r/monitor?op=count&timestamp=2026062720&group=test&domain=cat&key=myKey&value=1
旧: http://localhost:8080/cat/r/monitor?op=batch&batch=test
新: http://localhost:8080/cat/mvc/r/monitor?op=batch&batch=test
```

### 3.3 枚举存在但模块未注册页面

以下页面在枚举中存在，但当前旧模块未注册 Handler。暂不作为主迁移缺口，但删除枚举和旧模块前必须确认没有外部链接或隐藏入口：

```text
/r/network
/r/app
/r/browser
/r/server
/r/appstats
/r/crash
/r/applog
/s/web
/s/app
```

## 4. 执行阶段

### 阶段 0：建立迁移基线

目标：把当前事实固化，防止后续迁移时漏页面。

任务：

1. 为 `SpringMvcMigrationServlet` 的白名单路由建立自动化覆盖测试。
2. 建立旧模块注册页面清单测试，确保 `ReportModule`、`SystemModule` 中的 Handler 都能被发现。
3. 建立差异测试：已注册旧页面必须有 `/mvc` 路由或显式标记为废弃。
4. 对示例 URL `/mvc/r/t?...op=view` 增加 smoke test，确认走 `SpringMvcTransactionController`。

完成标准：

1. 能自动输出“已覆盖路由”和“缺失路由”。
2. 缺失清单与本文档 3.2 一致。
3. 新增迁移页面时测试会强制更新清单。

### 阶段 1：补齐 `/mvc` 页面和接口

目标：所有旧注册页面都有 Spring 新链路。

优先级顺序：

1. `/s/permission`
   - 状态：已完成 `/mvc/s/permission` 新链路。
   - 已覆盖 `op=user/resource/error`。

2. `/r/overload`
   - 状态：已完成 `/mvc/r/overload` 新链路。
   - 已覆盖 `op=view`，复用 `TableCapacityService`，Spring JSP 不再使用 Unidal taglib/WebRes。

3. 报表只读页面：
   - `/r/matrix`
   - `/r/model`
   - `/r/cache`
   - `/r/statistics`
   - `/r/storage`
   - 状态：以上页面均已完成 `/mvc` 新链路。

4. 配置或写操作页面：
   - `/r/alteration`
   - `/r/alert`
   - `/r/dependency`
   - 状态：`/r/monitor` 已完成 `/mvc` 新链路。

执行要求：

1. 每迁移一个页面，新建或扩展 `SpringMvc*Controller`。
2. 页面入口统一注册到 `SpringMvcMigrationServlet`。
3. JSP 优先放在 `jsp/spring/...`，不得继续使用旧 `JspViewer`。
4. 对同一路径下的 `op` 参数逐项核对旧 `Action.java`，不能只迁移默认 `view`。
5. 对写操作必须确认 GET/POST 行为与旧页面一致。

完成标准：

1. 本文档 3.2 的缺失路由全部清空。
2. 所有已注册旧页面都有 `/mvc` 新链路。
3. 关键 `op` 参数有 smoke test 或 controller unit test。

### 阶段 2：切换主路径 `/r/*` 和 `/s/*`

目标：让外部主路径不再进入 `SpringMvcRuntime`。

可选方案：

1. 推荐方案：将 `/r/*`、`/s/*` 直接映射到新的 Spring 路由分发 Servlet。
   - 可以复用并改造 `SpringMvcMigrationServlet`，让它同时支持 `/mvc/*`、`/r/*`、`/s/*`。
   - 先保持 `/mvc/*` 兼容。

2. 过渡方案：`/r/*`、`/s/*` 做 302 或内部 forward 到 `/mvc/r/*`、`/mvc/s/*`。
   - 风险较低，但 URL 语义会继续保留迁移前缀。

执行要求：

1. 修改 `web.xml` 时，确保 Filter 顺序和行为不回退。
2. `/r/*`、`/s/*` 主路径切换后，`SpringMvcServlet` 不应再处理线上请求。
3. 保留 `/mvc/*` 一段时间作为回滚入口。

完成标准：

1. `/r/t?...op=view` 和 `/mvc/r/t?...op=view` 都走同一套 Spring Controller。
2. 访问日志中不再出现 `SpringMvcRuntime` 处理业务页面。
3. 权限、登录、错误页、域名过滤行为保持一致。

### 阶段 3：清理 JSP 与 taglib 依赖

目标：页面渲染不再依赖 Unidal JSP/WebRes。

任务：

1. 扫描并替换所有 JSP/tag 文件中的 Unidal taglib：
   - `http://www.unidal.org/web/core`
   - `http://www.unidal.org/webres`
   - `/WEB-INF/app.tld`

2. 替换常见函数：
   - `w:format` -> JSTL `fmt:formatNumber` 或 Java 预格式化字段。
   - `w:size` / `w:length` -> JSTL `fn:length` 或 Controller 预计算。
   - `a:uri` / `a:action` -> Controller 提供 `baseUri`，JSP 拼接 Spring URL。
   - `res:*` -> 静态资源普通 `<link>`、`<script>`、`<img>`。

3. 删除不再使用的 tld：
   - `WEB-INF/app.tld`
   - `WEB-INF/web-core.tld`
   - `WEB-INF/webres.tld`

完成标准：

1. `cat-home/src/main/webapp` 中搜索不到 `org.unidal.web`、`http://www.unidal.org/web`。
2. 所有 Spring JSP 仍能正常渲染。
3. 不再需要 `WebResServer`。

### 阶段 4：删除旧 Unidal MVC 代码

目标：移除 Web MVC runtime 和旧页面模型。

任务：

1. 删除或隔离不再使用的入口：
   - `SpringMvcServlet`
   - `SpringMvcRuntime`
   - `ReportModule`
   - `SystemModule`
   - `ReportContext`
   - `SystemContext`

2. 删除旧页面组件：
   - `report/page/*/Handler.java`
   - `report/page/*/Payload.java`
   - `report/page/*/Model.java`
   - `report/page/*/JspViewer.java`
   - `system/page/*/Handler.java`
   - `system/page/*/Payload.java`
   - `system/page/*/Model.java`
   - `system/page/*/JspViewer.java`

3. 保留仍被 Spring Controller 复用的纯业务类。
   - 例如 `DisplayNames`、`DisplayTypes`、`TransactionGraphBuilder`、`TransactionMergeHelper` 这类无 Unidal 依赖的 helper 可以继续保留。

完成标准：

1. 生产代码中搜索不到 `org.unidal.web.mvc`。
2. 生产代码中搜索不到 `PageHandler`、`ActionPayload`、`ViewModel`、`BaseJspViewer`。
3. 编译通过。

### 阶段 5：删除 Maven 依赖

目标：从构建层移除 Web 相关 Unidal 依赖。

任务：

1. 从 `cat-home/pom.xml` 删除：
   - `org.unidal.framework:web-framework`
   - `org.unidal.webres:WebResServer`

2. 从根 `pom.xml` 的 dependency management 中删除不再使用的 Web 依赖。

3. 保留非 Web 的 Unidal 依赖，直到对应模块单独完成迁移。
   - 例如 `org.unidal.cat.message.storage.*` 属于消息存储链路，不应混入 Web MVC 删除批次。

完成标准：

1. `mvn -pl cat-home -am test` 通过。
2. `mvn dependency:tree` 中 `cat-home` 不再包含 `web-framework` 和 `WebResServer`。
3. Web 页面 smoke test 通过。

## 5. 每次迁移的标准流程

每迁移一个页面或接口，按以下顺序执行：

1. 读旧入口：
   - `Action.java`
   - `Payload.java`
   - `Handler.java`
   - `Model.java`
   - `JspViewer.java`

2. 列出旧路径和全部 `op`。

3. 新建或扩展 Spring Controller。

4. 新建或改造 `jsp/spring/...` 页面。

5. 注册 `/mvc` 路由。

6. 加测试：
   - 路由存在测试。
   - 默认页面 smoke test。
   - 关键 `op` 行为测试。

7. 扫描确认新路径主链路不引用：
   - `org.unidal.web.mvc`
   - `org.unidal.webres`
   - `BaseJspViewer`
   - `PageHandler`

8. 更新本文档对应清单。

## 6. 禁止事项

1. 不要在缺失页面补齐前删除 `SpringMvcRuntime`。
2. 不要在 JSP taglib 清理前删除 `WebResServer`。
3. 不要把 `org.unidal.cat.message.storage.*` 与 Web MVC 删除混为一谈。
4. 不要只迁移默认 `op=view`，必须检查旧 `Action.java` 的全部 action。
5. 不要让新 Spring JSP 引入 `/WEB-INF/app.tld`、`web-core.tld`、`webres.tld`。

## 7. 当前下一步

建议下一次实际改代码从阶段 0 开始：

1. 增加路由覆盖测试。
2. 固化当前 11 个缺失路由。
3. 然后优先迁移 `/s/permission`。
