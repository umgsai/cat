package com.dianping.cat.home.spring;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.analysis.ContainerMessageAnalyzerFactory;
import com.dianping.cat.analysis.DefaultMessageAnalyzerManager;
import com.dianping.cat.analysis.DefaultMessageHandler;
import com.dianping.cat.analysis.RealtimeConsumer;
import com.dianping.cat.analysis.TcpSocketReceiver;
import com.dianping.cat.config.AtomicMessageConfigManager;
import com.dianping.cat.config.ReportReloadConfigManager;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.config.content.LocalResourceContentFetcher;
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.config.transaction.TpValueStatisticConfigManager;
import com.dianping.cat.consumer.DatabaseParser;
import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.BusinessDelegate;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.CrossDelegate;
import com.dianping.cat.consumer.cross.IpConvertManager;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyDelegate;
import com.dianping.cat.consumer.dump.DumpAnalyzer;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.EventDelegate;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatDelegate;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixDelegate;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.ProblemDelegate;
import com.dianping.cat.consumer.problem.DefaultProblemHandler;
import com.dianping.cat.consumer.problem.LongExecutionProblemHandler;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.StateDelegate;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.StorageDelegate;
import com.dianping.cat.consumer.storage.StorageReportUpdater;
import com.dianping.cat.consumer.storage.builder.StorageBuilderManager;
import com.dianping.cat.consumer.storage.builder.StorageCacheBuilder;
import com.dianping.cat.consumer.storage.builder.StorageRPCBuilder;
import com.dianping.cat.consumer.storage.builder.StorageSQLBuilder;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.TopDelegate;
import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionDelegate;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.config.AlertPolicyManager;
import com.dianping.cat.alarm.spi.config.SenderConfigManager;
import com.dianping.cat.alarm.spi.decorator.DecoratorManager;
import com.dianping.cat.alarm.spi.receiver.ContactorManager;
import com.dianping.cat.alarm.spi.rule.DataChecker;
import com.dianping.cat.alarm.spi.rule.DefaultDataChecker;
import com.dianping.cat.alarm.service.AlertService;
import com.dianping.cat.alarm.spi.sender.MailSender;
import com.dianping.cat.alarm.spi.sender.SenderManager;
import com.dianping.cat.alarm.spi.sender.SmsSender;
import com.dianping.cat.alarm.spi.sender.WeixinSender;
import com.dianping.cat.alarm.spi.spliter.DXSpliter;
import com.dianping.cat.alarm.spi.spliter.MailSpliter;
import com.dianping.cat.alarm.spi.spliter.SmsSpliter;
import com.dianping.cat.alarm.spi.spliter.SpliterManager;
import com.dianping.cat.alarm.spi.spliter.WeixinSpliter;
import com.dianping.cat.mybatis.repository.alert.AlertRepository;
import com.dianping.cat.mybatis.AlertSummaryRepository;
import com.dianping.cat.mybatis.AlterationRepository;
import com.dianping.cat.mybatis.BaselineRepository;
import com.dianping.cat.mybatis.mapper.BusinessConfigRepository;
import com.dianping.cat.mybatis.ConfigModificationRepository;
import com.dianping.cat.mybatis.DailyReportContentRepository;
import com.dianping.cat.mybatis.HostInfoRepository;
import com.dianping.cat.mybatis.HourlyReportContentRepository;
import com.dianping.cat.mybatis.HourlyReportRepository;
import com.dianping.cat.mybatis.MetricGraphRepository;
import com.dianping.cat.mybatis.MetricScreenRepository;
import com.dianping.cat.mybatis.MonthlyReportContentRepository;
import com.dianping.cat.mybatis.MonthlyReportRepository;
import com.dianping.cat.mybatis.OverloadRepository;
import com.dianping.cat.mybatis.ProjectRepository;
import com.dianping.cat.mybatis.repository.server.alarm.rule.ServerAlarmRuleRepository;
import com.dianping.cat.mybatis.TaskRepository;
import com.dianping.cat.mybatis.TopologyGraphRepository;
import com.dianping.cat.mybatis.repository.user.define.rule.UserDefineRuleRepository;
import com.dianping.cat.mybatis.WeeklyReportContentRepository;
import com.dianping.cat.mybatis.WeeklyReportRepository;
import com.dianping.cat.mybatis.DailyReportRepository;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.spring.storage.SpringStorageComponentConfiguration;
import com.dianping.cat.message.DefaultPathBuilder;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.mvc.ReportModelDependencies;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.report.alert.config.BaseRuleHelper;
import com.dianping.cat.report.alert.business.BusinessAlert;
import com.dianping.cat.report.alert.business.BusinessContactor;
import com.dianping.cat.report.alert.business.BusinessDecorator;
import com.dianping.cat.report.alert.business.BusinessReportGroupService;
import com.dianping.cat.report.alert.business.BusinessRuleConfigManager;
import com.dianping.cat.report.alert.event.EventAlert;
import com.dianping.cat.report.alert.event.EventContactor;
import com.dianping.cat.report.alert.event.EventDecorator;
import com.dianping.cat.report.alert.event.EventRuleConfigManager;
import com.dianping.cat.report.alert.exception.ExceptionAlert;
import com.dianping.cat.report.alert.exception.AlertExceptionBuilder;
import com.dianping.cat.report.alert.exception.ExceptionContactor;
import com.dianping.cat.report.alert.exception.ExceptionDecorator;
import com.dianping.cat.report.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatContactor;
import com.dianping.cat.report.alert.heartbeat.HeartbeatDecorator;
import com.dianping.cat.report.alert.heartbeat.HeartbeatRuleConfigManager;
import com.dianping.cat.report.alert.spi.config.UserDefinedRuleManager;
import com.dianping.cat.report.alert.summary.AlertSummaryExecutor;
import com.dianping.cat.report.alert.summary.AlertSummaryService;
import com.dianping.cat.report.alert.summary.build.AlertInfoBuilder;
import com.dianping.cat.report.alert.summary.build.AlterationSummaryBuilder;
import com.dianping.cat.report.alert.summary.build.FailureSummaryBuilder;
import com.dianping.cat.report.alert.summary.build.RelatedSummaryBuilder;
import com.dianping.cat.report.alert.summary.build.SummaryBuilder;
import com.dianping.cat.report.alert.transaction.TransactionAlert;
import com.dianping.cat.report.alert.transaction.TransactionContactor;
import com.dianping.cat.report.alert.transaction.TransactionDecorator;
import com.dianping.cat.report.alert.transaction.TransactionRuleConfigManager;
import com.dianping.cat.report.alert.AlarmManager;
import com.dianping.cat.report.DomainValidator;
import com.dianping.cat.report.graph.svg.DefaultGraphBuilder;
import com.dianping.cat.report.graph.svg.DefaultValueTranslater;
import com.dianping.cat.report.graph.svg.ValueTranslater;
import com.dianping.cat.report.graph.metric.DataExtractor;
import com.dianping.cat.report.graph.metric.impl.DataExtractorImpl;
import com.dianping.cat.report.manager.BusinessReportManager;
import com.dianping.cat.report.manager.CrossReportManager;
import com.dianping.cat.report.manager.DependencyReportManager;
import com.dianping.cat.report.manager.EventReportManager;
import com.dianping.cat.report.manager.HeartbeatReportManager;
import com.dianping.cat.report.manager.MatrixReportManager;
import com.dianping.cat.report.manager.ProblemReportManager;
import com.dianping.cat.report.manager.StateReportManager;
import com.dianping.cat.report.manager.StorageReportManager;
import com.dianping.cat.report.manager.TopReportManager;
import com.dianping.cat.report.manager.TransactionReportManager;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.dependency.config.TopoGraphFormatConfigManager;
import com.dianping.cat.report.page.dependency.ExternalInfoBuilder;
import com.dianping.cat.report.page.dependency.graph.DependencyItemBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphConfigManager;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;
import com.dianping.cat.report.page.business.graph.BusinessDataFetcher;
import com.dianping.cat.report.page.business.graph.BusinessGraphCreator;
import com.dianping.cat.report.page.business.graph.CustomDataCalculator;
import com.dianping.cat.report.page.business.service.CachedBusinessReportService;
import com.dianping.cat.report.page.business.service.CompositeBusinessService;
import com.dianping.cat.report.page.business.service.HistoricalBusinessService;
import com.dianping.cat.report.page.business.service.LocalBusinessService;
import com.dianping.cat.report.page.business.service.BusinessReportService;
import com.dianping.cat.report.page.business.task.BusinessBaselineReportBuilder;
import com.dianping.cat.report.page.business.task.BusinessKeyHelper;
import com.dianping.cat.report.page.business.task.BusinessPointParser;
import com.dianping.cat.report.page.cross.service.CompositeCrossService;
import com.dianping.cat.report.page.cross.service.CrossReportService;
import com.dianping.cat.report.page.cross.service.HistoricalCrossService;
import com.dianping.cat.report.page.cross.service.LocalCrossService;
import com.dianping.cat.report.page.cross.task.CrossReportBuilder;
import com.dianping.cat.report.page.dependency.service.DependencyReportService;
import com.dianping.cat.report.page.dependency.service.CompositeDependencyService;
import com.dianping.cat.report.page.dependency.service.HistoricalDependencyService;
import com.dianping.cat.report.page.dependency.service.LocalDependencyService;
import com.dianping.cat.report.page.dependency.task.DependencyReportBuilder;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.report.page.event.service.CompositeEventService;
import com.dianping.cat.report.page.event.service.HistoricalEventService;
import com.dianping.cat.report.page.event.task.EventReportBuilder;
import com.dianping.cat.report.page.event.transform.EventMergeHelper;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.report.page.heartbeat.service.CompositeHeartbeatService;
import com.dianping.cat.report.page.heartbeat.service.HeartbeatReportService;
import com.dianping.cat.report.page.heartbeat.service.HistoricalHeartbeatService;
import com.dianping.cat.report.page.heartbeat.service.LocalHeartbeatService;
import com.dianping.cat.report.page.heartbeat.task.HeartbeatReportBuilder;
import com.dianping.cat.report.page.logview.service.CompositeLogViewService;
import com.dianping.cat.report.page.logview.service.HistoricalMessageService;
import com.dianping.cat.report.page.logview.service.LocalMessageService;
import com.dianping.cat.report.page.matrix.service.CompositeMatrixService;
import com.dianping.cat.report.page.matrix.service.HistoricalMatrixService;
import com.dianping.cat.report.page.matrix.service.MatrixReportService;
import com.dianping.cat.report.page.matrix.service.LocalMatrixService;
import com.dianping.cat.report.page.matrix.task.MatrixReportBuilder;
import com.dianping.cat.report.page.metric.service.BaselineService;
import com.dianping.cat.report.page.metric.service.DefaultBaselineService;
import com.dianping.cat.report.page.metric.task.BaselineConfigManager;
import com.dianping.cat.report.page.metric.task.BaselineCreator;
import com.dianping.cat.report.page.metric.task.DefaultBaselineCreator;
import com.dianping.cat.report.page.overload.task.CapacityUpdateStatusManager;
import com.dianping.cat.report.page.overload.task.CapacityUpdateTask;
import com.dianping.cat.report.page.overload.task.CapacityUpdater;
import com.dianping.cat.report.page.overload.task.DailyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.HourlyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.MonthlyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.TableCapacityService;
import com.dianping.cat.report.page.overload.task.WeeklyCapacityUpdater;
import com.dianping.cat.report.page.event.service.LocalEventService;
import com.dianping.cat.report.page.problem.service.CompositeProblemService;
import com.dianping.cat.report.page.problem.service.HistoricalProblemService;
import com.dianping.cat.report.page.problem.service.LocalProblemService;
import com.dianping.cat.report.page.problem.service.ProblemReportService;
import com.dianping.cat.report.page.problem.task.ProblemReportBuilder;
import com.dianping.cat.report.page.statistics.service.ClientReportService;
import com.dianping.cat.report.page.statistics.service.HeavyReportService;
import com.dianping.cat.report.page.statistics.service.JarReportService;
import com.dianping.cat.report.page.statistics.service.ServiceReportService;
import com.dianping.cat.report.page.statistics.service.UtilizationReportService;
import com.dianping.cat.report.page.statistics.task.heavy.HeavyReportBuilder;
import com.dianping.cat.report.page.statistics.task.jar.JarReportBuilder;
import com.dianping.cat.report.page.statistics.task.service.ClientReportBuilder;
import com.dianping.cat.report.page.statistics.task.service.ServiceReportBuilder;
import com.dianping.cat.report.page.statistics.task.utilization.UtilizationReportBuilder;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager;
import com.dianping.cat.report.page.storage.display.StorageAlertInfoBuilder;
import com.dianping.cat.report.page.storage.service.CompositeStorageService;
import com.dianping.cat.report.page.storage.service.HistoricalStorageService;
import com.dianping.cat.report.page.storage.service.LocalStorageService;
import com.dianping.cat.report.page.storage.task.StorageReportBuilder;
import com.dianping.cat.report.page.storage.task.StorageReportService;
import com.dianping.cat.report.page.storage.transform.StorageMergeHelper;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.report.server.ServersUpdater;
import com.dianping.cat.report.server.ServersUpdaterManager;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.task.DefaultRemoteServersUpdater;
import com.dianping.cat.report.task.ReportFacade;
import com.dianping.cat.report.task.current.CurrentReportBuilder;
import com.dianping.cat.report.task.cmdb.CmdbInfoReloadBuilder;
import com.dianping.cat.report.task.cmdb.ProjectUpdateTask;
import com.dianping.cat.report.page.state.service.LocalStateService;
import com.dianping.cat.report.page.state.service.CompositeStateService;
import com.dianping.cat.report.page.state.service.HistoricalStateService;
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.report.page.state.task.StateReportBuilder;
import com.dianping.cat.report.page.top.service.CompositeTopService;
import com.dianping.cat.report.page.top.service.HistoricalTopService;
import com.dianping.cat.report.page.top.service.LocalTopService;
import com.dianping.cat.report.page.top.service.TopReportService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.page.transaction.service.CompositeTransactionService;
import com.dianping.cat.report.page.transaction.service.HistoricalTransactionService;
import com.dianping.cat.report.page.transaction.service.LocalTransactionService;
import com.dianping.cat.report.page.transaction.task.TransactionReportBuilder;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.task.reload.ReportReloadTask;
import com.dianping.cat.report.task.reload.impl.BusinessReportReloader;
import com.dianping.cat.report.task.reload.impl.CrossReportReloader;
import com.dianping.cat.report.task.reload.impl.DependencyReportReloader;
import com.dianping.cat.report.task.reload.impl.EventReportReloader;
import com.dianping.cat.report.task.reload.impl.HeartbeatReportReloader;
import com.dianping.cat.report.task.reload.impl.MatrixReportReloader;
import com.dianping.cat.report.task.reload.impl.ProblemReportReloader;
import com.dianping.cat.report.task.reload.impl.StateReportReloader;
import com.dianping.cat.report.task.reload.impl.StorageReportReloader;
import com.dianping.cat.report.task.reload.impl.TopReportReloader;
import com.dianping.cat.report.task.reload.impl.TransactionReportReloader;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.statistic.ServerStatisticManager;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.login.service.CookieManager;
import com.dianping.cat.system.page.login.service.DefaultCatPropertyProvider;
import com.dianping.cat.system.page.login.service.SessionManager;
import com.dianping.cat.system.page.login.service.SigninService;
import com.dianping.cat.system.page.login.service.TokenBuilder;
import com.dianping.cat.system.page.login.service.TokenManager;
import com.dianping.cat.system.page.permission.ResourceConfigManager;
import com.dianping.cat.system.page.permission.UserConfigManager;
import com.dianping.cat.system.page.router.config.RouterConfigAdjustor;
import com.dianping.cat.system.page.router.config.RouterConfigHandler;
import com.dianping.cat.system.page.router.config.RouterConfigManager;
import com.dianping.cat.system.page.router.service.CachedRouterConfigService;
import com.dianping.cat.system.page.router.service.RouterConfigService;
import com.dianping.cat.system.page.router.task.RouterConfigBuilder;

@Configuration
@Import({SpringMvcMigrationConfiguration.class, SpringStorageComponentConfiguration.class})
@ComponentScan(basePackageClasses = {BusinessAnalyzer.class, BusinessDelegate.class,
		TransactionAnalyzer.class, TransactionDelegate.class, CrossAnalyzer.class, CrossDelegate.class,
		DumpAnalyzer.class, DependencyAnalyzer.class, DependencyDelegate.class, EventAnalyzer.class, EventDelegate.class,
		HeartbeatAnalyzer.class, HeartbeatDelegate.class, MatrixAnalyzer.class, MatrixDelegate.class,
		ProblemAnalyzer.class, ProblemDelegate.class, StorageAnalyzer.class, StorageDelegate.class,
		StorageReportUpdater.class, StorageBuilderManager.class, TopAnalyzer.class, TopDelegate.class, StateAnalyzer.class, StateDelegate.class,
		ContainerMessageAnalyzerFactory.class, DefaultMessageAnalyzerManager.class, RealtimeConsumer.class,
		DefaultMessageHandler.class, TcpSocketReceiver.class, CatHomeRuntimeBootstrap.class,
		CatHomeSpringStartupVerifier.class, BusinessKeyHelper.class, BusinessDataFetcher.class,
		CachedBusinessReportService.class, BusinessReportGroupService.class, CustomDataCalculator.class,
		BusinessPointParser.class, DomainGroupConfigManager.class, StorageGroupConfigManager.class,
		HeartbeatDisplayPolicyManager.class, BaselineConfigManager.class, DefaultBaselineCreator.class,
		BusinessGraphCreator.class, PayloadNormalizer.class, ReportModelDependencies.class, JsonBuilder.class,
		DomainValidator.class, DefaultValueTranslater.class, DefaultGraphBuilder.class, DependencyItemBuilder.class,
		TopologyGraphBuilder.class, TopologyGraphManager.class, TopologyGraphConfigManager.class,
		TopoGraphFormatConfigManager.class, StorageAlertInfoBuilder.class, ExternalInfoBuilder.class, StorageMergeHelper.class,
		StateReportService.class, StateReportBuilder.class, EventReportService.class, EventReportBuilder.class,
		HeartbeatReportService.class, HeartbeatReportBuilder.class, DependencyReportService.class,
		DependencyReportBuilder.class, MatrixReportService.class, MatrixReportBuilder.class,
		TransactionReportService.class, TransactionReportBuilder.class, TopReportService.class,
		CrossReportService.class, CrossReportBuilder.class, ProblemReportService.class, ProblemReportBuilder.class,
		StorageReportService.class, StorageReportBuilder.class, BusinessReportService.class,
		BusinessReportManager.class, TransactionReportManager.class, CrossReportManager.class,
		DependencyReportManager.class, EventReportManager.class, HeartbeatReportManager.class,
		MatrixReportManager.class, ProblemReportManager.class, StorageReportManager.class,
		TopReportManager.class, StateReportManager.class,
		ConfigRepository.class, BusinessConfigRepository.class, ProjectRepository.class, HostInfoRepository.class,
		DailyReportRepository.class, DailyReportContentRepository.class, HourlyReportRepository.class,
		HourlyReportContentRepository.class, WeeklyReportRepository.class, WeeklyReportContentRepository.class,
		MonthlyReportRepository.class, MonthlyReportContentRepository.class, OverloadRepository.class,
		AlertRepository.class, AlterationRepository.class, BaselineRepository.class, TopologyGraphRepository.class,
		TaskRepository.class, AlertSummaryRepository.class, ConfigModificationRepository.class,
		MetricGraphRepository.class, MetricScreenRepository.class, ServerAlarmRuleRepository.class,
		UserDefineRuleRepository.class,
		HistoricalProblemService.class, HistoricalBusinessService.class, HistoricalEventService.class,
		HistoricalTransactionService.class, HistoricalHeartbeatService.class, HistoricalTopService.class,
		HistoricalStateService.class, HistoricalStorageService.class, HistoricalCrossService.class,
		HistoricalMatrixService.class, HistoricalDependencyService.class, CompositeProblemService.class,
		CompositeBusinessService.class, CompositeEventService.class, CompositeTransactionService.class,
		CompositeHeartbeatService.class, CompositeTopService.class, CompositeStateService.class,
		CompositeStorageService.class, CompositeCrossService.class, CompositeMatrixService.class,
		CompositeDependencyService.class, LocalProblemService.class, LocalEventService.class,
		LocalTransactionService.class, LocalHeartbeatService.class, LocalCrossService.class,
		LocalMatrixService.class, LocalDependencyService.class, LocalTopService.class,
		LocalStateService.class, LocalStorageService.class, LocalBusinessService.class,
		LocalMessageService.class, HistoricalMessageService.class,
		CompositeLogViewService.class,
		BusinessBaselineReportBuilder.class, JarReportService.class, JarReportBuilder.class,
		HeavyReportService.class, HeavyReportBuilder.class, ClientReportService.class, ClientReportBuilder.class,
		ServiceReportService.class, ServiceReportBuilder.class, UtilizationReportService.class,
		UtilizationReportBuilder.class, CapacityUpdateStatusManager.class, HourlyCapacityUpdater.class,
		DailyCapacityUpdater.class, WeeklyCapacityUpdater.class, MonthlyCapacityUpdater.class,
		CapacityUpdateTask.class, TableCapacityService.class, RouterConfigService.class,
		CachedRouterConfigService.class, RouterConfigManager.class, RouterConfigHandler.class,
		RouterConfigAdjustor.class, RouterConfigBuilder.class, DatabaseParser.class, IpConvertManager.class,
		StorageSQLBuilder.class, StorageCacheBuilder.class, StorageRPCBuilder.class,
		DefaultProblemHandler.class, LongExecutionProblemHandler.class,
		AlertSummaryService.class, RelatedSummaryBuilder.class, FailureSummaryBuilder.class,
		AlterationSummaryBuilder.class, AlertSummaryExecutor.class, AlertService.class, DefaultDataChecker.class, BaseRuleHelper.class,
		AlertInfoBuilder.class, UserDefinedRuleManager.class, DefaultBaselineService.class,
		DataExtractorImpl.class, EventMergeHelper.class, TransactionMergeHelper.class,
		LocalResourceContentFetcher.class, DefaultPathBuilder.class, ServerStatisticManager.class,
		AllReportConfigManager.class, ServerConfigManager.class, ServerFilterConfigManager.class, SampleConfigManager.class,
		ReportReloadConfigManager.class, AtomicMessageConfigManager.class, TpValueStatisticConfigManager.class,
		BusinessConfigManager.class,
		BusinessReportReloader.class, TransactionReportReloader.class, CrossReportReloader.class,
		DependencyReportReloader.class, EventReportReloader.class, HeartbeatReportReloader.class,
		MatrixReportReloader.class, ProblemReportReloader.class, StorageReportReloader.class,
		TopReportReloader.class, StateReportReloader.class,
		UserConfigManager.class, ResourceConfigManager.class, CookieManager.class, TokenBuilder.class,
		DefaultCatPropertyProvider.class, TokenManager.class, SessionManager.class, SigninService.class,
		ProjectService.class, HostinfoService.class, TaskManager.class, DefaultTaskConsumer.class, ReportFacade.class,
		CurrentReportBuilder.class, ProjectUpdateTask.class, CmdbInfoReloadBuilder.class, ReportReloadTask.class,
		AlertExceptionBuilder.class, BusinessAlert.class, EventAlert.class, ExceptionAlert.class,
		HeartbeatAlert.class, TransactionAlert.class, ExceptionRuleConfigManager.class, TransactionRuleConfigManager.class,
		EventRuleConfigManager.class, HeartbeatRuleConfigManager.class, BusinessRuleConfigManager.class,
		BusinessTagConfigManager.class, AlertConfigManager.class, AlertPolicyManager.class, SenderConfigManager.class,
		MailSender.class, SmsSender.class, WeixinSender.class,
		MailSpliter.class, SmsSpliter.class, WeixinSpliter.class, DXSpliter.class,
		BusinessContactor.class, EventContactor.class, ExceptionContactor.class,
		HeartbeatContactor.class, TransactionContactor.class, BusinessDecorator.class,
		EventDecorator.class, ExceptionDecorator.class, HeartbeatDecorator.class, TransactionDecorator.class,
		SenderManager.class, SpliterManager.class, ContactorManager.class, DecoratorManager.class,
		RemoteServersManager.class, DefaultRemoteServersUpdater.class, ServersUpdaterManager.class,
		com.dianping.cat.alarm.spi.decorator.RuleFTLDecorator.class,
		com.dianping.cat.alarm.spi.AlertManager.class, AlarmManager.class,
		com.dianping.cat.report.page.home.JspViewer.class,
		com.dianping.cat.report.page.monitor.JspViewer.class,
		com.dianping.cat.report.page.model.JspViewer.class,
		com.dianping.cat.report.page.alteration.JspViewer.class,
		com.dianping.cat.report.page.alert.JspViewer.class,
		com.dianping.cat.report.page.cache.JspViewer.class,
		com.dianping.cat.report.page.event.JspViewer.class,
		com.dianping.cat.report.page.transaction.JspViewer.class,
		com.dianping.cat.report.page.transaction.XmlViewer.class,
		com.dianping.cat.report.page.problem.JspViewer.class,
		com.dianping.cat.report.page.heartbeat.JspViewer.class,
		com.dianping.cat.report.page.top.JspViewer.class,
		com.dianping.cat.report.page.business.JspViewer.class,
		com.dianping.cat.report.page.logview.JspViewer.class,
		com.dianping.cat.report.page.state.JspViewer.class,
		com.dianping.cat.report.page.storage.JspViewer.class,
		com.dianping.cat.report.page.dependency.JspViewer.class,
		com.dianping.cat.report.page.matrix.JspViewer.class,
		com.dianping.cat.report.page.statistics.JspViewer.class,
		com.dianping.cat.report.page.overload.JspViewer.class,
		com.dianping.cat.report.page.cross.JspViewer.class,
		com.dianping.cat.system.page.config.JspViewer.class,
		com.dianping.cat.system.page.business.JspViewer.class,
		com.dianping.cat.system.page.permission.JspViewer.class,
		com.dianping.cat.system.page.login.JspViewer.class,
		com.dianping.cat.system.page.plugin.JspViewer.class,
		com.dianping.cat.system.page.project.JspViewer.class,
		com.dianping.cat.report.page.monitor.Handler.class,
		com.dianping.cat.report.page.overload.Handler.class,
		com.dianping.cat.system.page.login.Handler.class,
		com.dianping.cat.system.page.plugin.Handler.class,
		com.dianping.cat.system.page.project.Handler.class,
		com.dianping.cat.report.page.home.Handler.class,
		com.dianping.cat.report.page.alteration.Handler.class,
		com.dianping.cat.report.page.alert.Handler.class,
		com.dianping.cat.report.page.model.Handler.class,
		com.dianping.cat.report.page.cache.Handler.class,
		com.dianping.cat.report.page.event.Handler.class,
		com.dianping.cat.report.page.transaction.Handler.class,
		com.dianping.cat.report.page.problem.Handler.class,
		com.dianping.cat.report.page.heartbeat.Handler.class,
		com.dianping.cat.report.page.business.Handler.class,
		com.dianping.cat.report.page.logview.Handler.class,
		com.dianping.cat.report.page.top.Handler.class,
		com.dianping.cat.report.page.state.Handler.class,
		com.dianping.cat.report.page.storage.Handler.class,
		com.dianping.cat.report.page.dependency.Handler.class,
		com.dianping.cat.report.page.statistics.Handler.class,
		com.dianping.cat.report.page.matrix.Handler.class,
		com.dianping.cat.report.page.cross.Handler.class,
		com.dianping.cat.system.page.config.Handler.class,
		com.dianping.cat.system.page.router.Handler.class,
		com.dianping.cat.system.page.business.Handler.class,
		com.dianping.cat.report.page.heartbeat.HistoryGraphs.class,
		com.dianping.cat.report.page.state.StateGraphBuilder.class,
		com.dianping.cat.report.page.state.StateBuilder.class,
		ConfigHtmlParser.class,
		com.dianping.cat.system.page.config.processor.GlobalConfigProcessor.class,
		com.dianping.cat.system.page.config.processor.DependencyConfigProcessor.class,
		com.dianping.cat.system.page.config.processor.ExceptionConfigProcessor.class,
		com.dianping.cat.system.page.config.processor.HeartbeatConfigProcessor.class,
		com.dianping.cat.system.page.config.processor.StorageConfigProcessor.class,
		com.dianping.cat.system.page.config.processor.TransactionConfigProcessor.class,
		com.dianping.cat.system.page.config.processor.EventConfigProcessor.class,
		com.dianping.cat.system.page.config.processor.AlertConfigProcessor.class,
		com.dianping.cat.system.page.permission.Handler.class},
		includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
				classes = {BusinessAnalyzer.class, BusinessDelegate.class,
					TransactionAnalyzer.class, TransactionDelegate.class, CrossAnalyzer.class, CrossDelegate.class,
					DumpAnalyzer.class, DependencyAnalyzer.class, DependencyDelegate.class,
					EventAnalyzer.class, EventDelegate.class, HeartbeatAnalyzer.class, HeartbeatDelegate.class,
					MatrixAnalyzer.class, MatrixDelegate.class, ProblemAnalyzer.class, ProblemDelegate.class,
					StorageAnalyzer.class, StorageDelegate.class, StorageReportUpdater.class,
					StorageBuilderManager.class,
					TopAnalyzer.class, TopDelegate.class, StateAnalyzer.class, StateDelegate.class,
					ContainerMessageAnalyzerFactory.class, DefaultMessageAnalyzerManager.class,
					RealtimeConsumer.class, DefaultMessageHandler.class, TcpSocketReceiver.class,
					CatHomeRuntimeBootstrap.class, CatHomeSpringStartupVerifier.class,
					BusinessKeyHelper.class, BusinessDataFetcher.class,
					CachedBusinessReportService.class, BusinessReportGroupService.class, CustomDataCalculator.class,
					BusinessPointParser.class, DomainGroupConfigManager.class, StorageGroupConfigManager.class,
					HeartbeatDisplayPolicyManager.class, BaselineConfigManager.class, DefaultBaselineCreator.class,
					BusinessGraphCreator.class, PayloadNormalizer.class, ReportModelDependencies.class,
					JsonBuilder.class, DomainValidator.class, DefaultValueTranslater.class, DefaultGraphBuilder.class,
					DependencyItemBuilder.class, TopologyGraphBuilder.class, TopologyGraphManager.class,
					TopologyGraphConfigManager.class, TopoGraphFormatConfigManager.class, StorageAlertInfoBuilder.class,
					ExternalInfoBuilder.class, StorageMergeHelper.class, StateReportService.class,
					StateReportBuilder.class, EventReportService.class, EventReportBuilder.class,
					HeartbeatReportService.class, HeartbeatReportBuilder.class, DependencyReportService.class,
					DependencyReportBuilder.class, MatrixReportService.class, MatrixReportBuilder.class,
					TransactionReportService.class, TransactionReportBuilder.class, TopReportService.class,
					CrossReportService.class, CrossReportBuilder.class, ProblemReportService.class,
					ProblemReportBuilder.class, StorageReportService.class, StorageReportBuilder.class,
					BusinessReportService.class, BusinessBaselineReportBuilder.class, JarReportService.class,
					BusinessReportManager.class, TransactionReportManager.class, CrossReportManager.class,
					DependencyReportManager.class, EventReportManager.class, HeartbeatReportManager.class,
					MatrixReportManager.class, ProblemReportManager.class, StorageReportManager.class,
					TopReportManager.class, StateReportManager.class,
					ConfigRepository.class, BusinessConfigRepository.class, ProjectRepository.class,
					HostInfoRepository.class, DailyReportRepository.class, DailyReportContentRepository.class,
					HourlyReportRepository.class, HourlyReportContentRepository.class, WeeklyReportRepository.class,
					WeeklyReportContentRepository.class, MonthlyReportRepository.class,
					MonthlyReportContentRepository.class, OverloadRepository.class, AlertRepository.class,
					AlterationRepository.class, BaselineRepository.class, TopologyGraphRepository.class,
					TaskRepository.class, AlertSummaryRepository.class, ConfigModificationRepository.class,
					MetricGraphRepository.class, MetricScreenRepository.class, ServerAlarmRuleRepository.class,
					UserDefineRuleRepository.class,
					HistoricalProblemService.class, HistoricalBusinessService.class, HistoricalEventService.class,
					HistoricalTransactionService.class, HistoricalHeartbeatService.class, HistoricalTopService.class,
					HistoricalStateService.class, HistoricalStorageService.class, HistoricalCrossService.class,
					HistoricalMatrixService.class, HistoricalDependencyService.class, CompositeProblemService.class,
					CompositeBusinessService.class, CompositeEventService.class, CompositeTransactionService.class,
					CompositeHeartbeatService.class, CompositeTopService.class, CompositeStateService.class,
					CompositeStorageService.class, CompositeCrossService.class, CompositeMatrixService.class,
					CompositeDependencyService.class, LocalProblemService.class, LocalEventService.class,
					LocalTransactionService.class, LocalHeartbeatService.class, LocalCrossService.class,
					LocalMatrixService.class, LocalDependencyService.class, LocalTopService.class,
					LocalStateService.class, LocalStorageService.class, LocalBusinessService.class,
					LocalMessageService.class, HistoricalMessageService.class,
					CompositeLogViewService.class,
					JarReportBuilder.class, HeavyReportService.class, HeavyReportBuilder.class,
					ClientReportService.class, ClientReportBuilder.class, ServiceReportService.class,
					ServiceReportBuilder.class, UtilizationReportService.class, UtilizationReportBuilder.class,
					CapacityUpdateStatusManager.class, HourlyCapacityUpdater.class, DailyCapacityUpdater.class,
					WeeklyCapacityUpdater.class, MonthlyCapacityUpdater.class, CapacityUpdateTask.class,
					TableCapacityService.class, RouterConfigService.class, CachedRouterConfigService.class,
					RouterConfigManager.class, RouterConfigHandler.class, RouterConfigAdjustor.class,
					RouterConfigBuilder.class, DatabaseParser.class,
					IpConvertManager.class, StorageSQLBuilder.class, StorageCacheBuilder.class, StorageRPCBuilder.class,
					DefaultProblemHandler.class, LongExecutionProblemHandler.class,
					AlertSummaryService.class, RelatedSummaryBuilder.class, FailureSummaryBuilder.class,
					AlterationSummaryBuilder.class, AlertSummaryExecutor.class, AlertService.class,
					DefaultDataChecker.class, BaseRuleHelper.class,
					AlertInfoBuilder.class, UserDefinedRuleManager.class, DefaultBaselineService.class,
					DataExtractorImpl.class, EventMergeHelper.class, TransactionMergeHelper.class,
					LocalResourceContentFetcher.class, DefaultPathBuilder.class, ServerStatisticManager.class,
					AllReportConfigManager.class, ServerConfigManager.class, ServerFilterConfigManager.class, SampleConfigManager.class,
					ReportReloadConfigManager.class, AtomicMessageConfigManager.class,
					TpValueStatisticConfigManager.class, BusinessConfigManager.class,
					BusinessReportReloader.class, TransactionReportReloader.class, CrossReportReloader.class,
					DependencyReportReloader.class, EventReportReloader.class, HeartbeatReportReloader.class,
					MatrixReportReloader.class, ProblemReportReloader.class, StorageReportReloader.class,
					TopReportReloader.class, StateReportReloader.class,
					UserConfigManager.class, ResourceConfigManager.class,
					CookieManager.class, TokenBuilder.class, DefaultCatPropertyProvider.class,
					TokenManager.class, SessionManager.class, SigninService.class, ProjectService.class,
					HostinfoService.class, TaskManager.class, DefaultTaskConsumer.class, ReportFacade.class,
					CurrentReportBuilder.class, ProjectUpdateTask.class, CmdbInfoReloadBuilder.class,
					ReportReloadTask.class,
					AlertExceptionBuilder.class, BusinessAlert.class, EventAlert.class, ExceptionAlert.class,
					HeartbeatAlert.class, TransactionAlert.class, ExceptionRuleConfigManager.class,
					TransactionRuleConfigManager.class, EventRuleConfigManager.class, HeartbeatRuleConfigManager.class,
					BusinessRuleConfigManager.class, BusinessTagConfigManager.class, AlertConfigManager.class,
					AlertPolicyManager.class, SenderConfigManager.class, MailSender.class, SmsSender.class, WeixinSender.class,
					MailSpliter.class, SmsSpliter.class, WeixinSpliter.class, DXSpliter.class,
					BusinessContactor.class, EventContactor.class, ExceptionContactor.class,
					HeartbeatContactor.class, TransactionContactor.class, BusinessDecorator.class,
					EventDecorator.class, ExceptionDecorator.class, HeartbeatDecorator.class, TransactionDecorator.class,
					SenderManager.class, SpliterManager.class, ContactorManager.class, DecoratorManager.class,
					RemoteServersManager.class, DefaultRemoteServersUpdater.class, ServersUpdaterManager.class,
					com.dianping.cat.alarm.spi.decorator.RuleFTLDecorator.class,
					com.dianping.cat.alarm.spi.AlertManager.class, AlarmManager.class,
					com.dianping.cat.report.page.home.JspViewer.class,
					com.dianping.cat.report.page.monitor.JspViewer.class,
					com.dianping.cat.report.page.model.JspViewer.class,
					com.dianping.cat.report.page.alteration.JspViewer.class,
					com.dianping.cat.report.page.alert.JspViewer.class,
					com.dianping.cat.report.page.cache.JspViewer.class,
					com.dianping.cat.report.page.event.JspViewer.class,
					com.dianping.cat.report.page.transaction.JspViewer.class,
					com.dianping.cat.report.page.transaction.XmlViewer.class,
					com.dianping.cat.report.page.problem.JspViewer.class,
					com.dianping.cat.report.page.heartbeat.JspViewer.class,
					com.dianping.cat.report.page.top.JspViewer.class,
					com.dianping.cat.report.page.business.JspViewer.class,
					com.dianping.cat.report.page.logview.JspViewer.class,
					com.dianping.cat.report.page.state.JspViewer.class,
					com.dianping.cat.report.page.storage.JspViewer.class,
					com.dianping.cat.report.page.dependency.JspViewer.class,
					com.dianping.cat.report.page.matrix.JspViewer.class,
					com.dianping.cat.report.page.statistics.JspViewer.class,
					com.dianping.cat.report.page.overload.JspViewer.class,
					com.dianping.cat.report.page.cross.JspViewer.class,
					com.dianping.cat.system.page.config.JspViewer.class,
					com.dianping.cat.system.page.business.JspViewer.class,
					com.dianping.cat.system.page.permission.JspViewer.class,
					com.dianping.cat.system.page.login.JspViewer.class,
					com.dianping.cat.system.page.plugin.JspViewer.class,
					com.dianping.cat.system.page.project.JspViewer.class,
					com.dianping.cat.report.page.monitor.Handler.class,
					com.dianping.cat.report.page.overload.Handler.class,
					com.dianping.cat.system.page.login.Handler.class,
					com.dianping.cat.system.page.plugin.Handler.class,
					com.dianping.cat.system.page.project.Handler.class,
					com.dianping.cat.report.page.home.Handler.class,
					com.dianping.cat.report.page.alteration.Handler.class,
					com.dianping.cat.report.page.alert.Handler.class,
					com.dianping.cat.report.page.model.Handler.class,
					com.dianping.cat.report.page.cache.Handler.class,
					com.dianping.cat.report.page.event.Handler.class,
					com.dianping.cat.report.page.transaction.Handler.class,
					com.dianping.cat.report.page.problem.Handler.class,
					com.dianping.cat.report.page.heartbeat.Handler.class,
					com.dianping.cat.report.page.business.Handler.class,
					com.dianping.cat.report.page.logview.Handler.class,
					com.dianping.cat.report.page.top.Handler.class,
					com.dianping.cat.report.page.state.Handler.class,
					com.dianping.cat.report.page.storage.Handler.class,
					com.dianping.cat.report.page.dependency.Handler.class,
					com.dianping.cat.report.page.statistics.Handler.class,
					com.dianping.cat.report.page.matrix.Handler.class,
					com.dianping.cat.report.page.cross.Handler.class,
					com.dianping.cat.system.page.config.Handler.class,
					com.dianping.cat.system.page.router.Handler.class,
					com.dianping.cat.system.page.business.Handler.class,
					com.dianping.cat.report.page.heartbeat.HistoryGraphs.class,
					com.dianping.cat.report.page.state.StateGraphBuilder.class,
					com.dianping.cat.report.page.state.StateBuilder.class,
					ConfigHtmlParser.class,
					com.dianping.cat.system.page.config.processor.GlobalConfigProcessor.class,
					com.dianping.cat.system.page.config.processor.DependencyConfigProcessor.class,
					com.dianping.cat.system.page.config.processor.ExceptionConfigProcessor.class,
					com.dianping.cat.system.page.config.processor.HeartbeatConfigProcessor.class,
					com.dianping.cat.system.page.config.processor.StorageConfigProcessor.class,
					com.dianping.cat.system.page.config.processor.TransactionConfigProcessor.class,
					com.dianping.cat.system.page.config.processor.EventConfigProcessor.class,
					com.dianping.cat.system.page.config.processor.AlertConfigProcessor.class,
					com.dianping.cat.system.page.permission.Handler.class}),
		useDefaultFilters = false)
@MapperScan(basePackages = {
		"com.dianping.cat.mybatis.mapper",
		"com.dianping.cat.mybatis.alert.dao",
		"com.dianping.cat.mybatis.server.alarm.rule.dao",
		"com.dianping.cat.mybatis.user.define.rule.dao"
})
public class CatHomeSpringConfiguration {
	@Bean
	public DataSource catDataSource() {
		return CatHomeSpringDataSourceFactory.createCatDataSource();
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource catDataSource) throws Exception {
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

		factory.setDataSource(catDataSource);
		factory.setMapperLocations(
				resolver.getResource("classpath:mybatis/mapper/ConfigMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/DailyReportMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/HostInfoMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/HourlyReportMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/WeeklyReportMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/MonthReportMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/ProjectMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/DailyReportContentMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/HourlyReportContentMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/WeeklyReportContentMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/MonthlyReportContentMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/BusinessConfigMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/TaskMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/AlertSummaryMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/ConfigModificationMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/BaselineMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/OverloadMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/TopologyGraphMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/MetricGraphMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/MetricScreenMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/AlterationMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/AlertMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/ServerAlarmRuleMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/UserDefineRuleMapper.xml"));
		return factory.getObject();
	}

	@Bean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	@Bean
	public PlatformTransactionManager transactionManager(DataSource catDataSource) {
		return new DataSourceTransactionManager(catDataSource);
	}

	@Bean
	public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
		return new TransactionTemplate(transactionManager);
	}
}
