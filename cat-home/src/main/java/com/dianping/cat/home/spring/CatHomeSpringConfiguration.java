package com.dianping.cat.home.spring;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.unidal.cat.message.storage.MessageFinderManager;
import org.unidal.cat.message.storage.internals.DefaultMessageFinderManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.config.AtomicMessageConfigManager;
import com.dianping.cat.config.ReportReloadConfigManager;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.config.content.LocalResourceContentFetcher;
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.config.transaction.TpValueStatisticConfigManager;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.config.repository.ConfigRepository;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.config.AlertPolicyManager;
import com.dianping.cat.alarm.spi.config.SenderConfigManager;
import com.dianping.cat.alarm.spi.decorator.Decorator;
import com.dianping.cat.alarm.spi.decorator.DecoratorManager;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.alarm.spi.receiver.ContactorManager;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;
import com.dianping.cat.alarm.spi.sender.MailSender;
import com.dianping.cat.alarm.spi.sender.Sender;
import com.dianping.cat.alarm.spi.sender.SenderManager;
import com.dianping.cat.alarm.spi.sender.SmsSender;
import com.dianping.cat.alarm.spi.sender.WeixinSender;
import com.dianping.cat.alarm.spi.spliter.DXSpliter;
import com.dianping.cat.alarm.spi.spliter.MailSpliter;
import com.dianping.cat.alarm.spi.spliter.SmsSpliter;
import com.dianping.cat.alarm.spi.spliter.Spliter;
import com.dianping.cat.alarm.spi.spliter.SpliterManager;
import com.dianping.cat.alarm.spi.spliter.WeixinSpliter;
import com.dianping.cat.core.mybatis.repository.alert.AlertRepository;
import com.dianping.cat.core.mybatis.repository.alert.summary.AlertSummaryRepository;
import com.dianping.cat.core.mybatis.repository.alteration.AlterationRepository;
import com.dianping.cat.core.mybatis.repository.baseline.BaselineRepository;
import com.dianping.cat.core.mybatis.repository.business.config.BusinessConfigRepository;
import com.dianping.cat.core.mybatis.repository.config.modification.ConfigModificationRepository;
import com.dianping.cat.core.mybatis.repository.daily.report.content.DailyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.hostinfo.HostinfoRepository;
import com.dianping.cat.core.mybatis.repository.hourly.report.content.HourlyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.hourlyreport.HourlyReportRepository;
import com.dianping.cat.core.mybatis.repository.metric.graph.MetricGraphRepository;
import com.dianping.cat.core.mybatis.repository.metric.screen.MetricScreenRepository;
import com.dianping.cat.core.mybatis.repository.monthly.report.content.MonthlyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.monthreport.MonthlyReportRepository;
import com.dianping.cat.core.mybatis.repository.overload.OverloadRepository;
import com.dianping.cat.core.mybatis.repository.project.ProjectRepository;
import com.dianping.cat.core.mybatis.repository.server.alarm.rule.ServerAlarmRuleRepository;
import com.dianping.cat.core.mybatis.repository.task.TaskRepository;
import com.dianping.cat.core.mybatis.repository.topologygraph.TopologyGraphRepository;
import com.dianping.cat.core.mybatis.repository.user.define.rule.UserDefineRuleRepository;
import com.dianping.cat.core.mybatis.repository.weekly.report.content.WeeklyReportContentRepository;
import com.dianping.cat.core.mybatis.repository.weeklyreport.WeeklyReportRepository;
import com.dianping.cat.core.report.daily.repository.DailyReportRepository;
import com.dianping.cat.message.DefaultPathBuilder;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.report.DefaultReportBucketManager;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.report.alert.config.BaseRuleHelper;
import com.dianping.cat.report.alert.business.BusinessContactor;
import com.dianping.cat.report.alert.business.BusinessDecorator;
import com.dianping.cat.report.alert.business.BusinessRuleConfigManager;
import com.dianping.cat.report.alert.event.EventContactor;
import com.dianping.cat.report.alert.event.EventDecorator;
import com.dianping.cat.report.alert.event.EventRuleConfigManager;
import com.dianping.cat.report.alert.exception.ExceptionContactor;
import com.dianping.cat.report.alert.exception.ExceptionDecorator;
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
import com.dianping.cat.report.alert.transaction.TransactionContactor;
import com.dianping.cat.report.alert.transaction.TransactionDecorator;
import com.dianping.cat.report.alert.transaction.TransactionRuleConfigManager;
import com.dianping.cat.report.alert.AlarmManager;
import com.dianping.cat.report.DomainValidator;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.dependency.config.TopoGraphFormatConfigManager;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphConfigManager;
import com.dianping.cat.report.page.business.graph.BusinessDataFetcher;
import com.dianping.cat.report.page.business.graph.CustomDataCalculator;
import com.dianping.cat.report.page.business.service.LocalBusinessService;
import com.dianping.cat.report.page.business.service.BusinessReportService;
import com.dianping.cat.report.page.business.task.BusinessKeyHelper;
import com.dianping.cat.report.page.business.task.BusinessPointParser;
import com.dianping.cat.report.page.cross.service.LocalCrossService;
import com.dianping.cat.report.page.dependency.service.LocalDependencyService;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.report.page.heartbeat.service.LocalHeartbeatService;
import com.dianping.cat.report.page.logview.service.LocalMessageService;
import com.dianping.cat.report.page.matrix.service.LocalMatrixService;
import com.dianping.cat.report.page.metric.service.BaselineService;
import com.dianping.cat.report.page.metric.service.DefaultBaselineService;
import com.dianping.cat.report.page.metric.task.BaselineConfigManager;
import com.dianping.cat.report.page.metric.task.BaselineCreator;
import com.dianping.cat.report.page.metric.task.DefaultBaselineCreator;
import com.dianping.cat.report.page.event.service.LocalEventService;
import com.dianping.cat.report.page.problem.service.CompositeProblemService;
import com.dianping.cat.report.page.problem.service.HistoricalProblemService;
import com.dianping.cat.report.page.problem.service.LocalProblemService;
import com.dianping.cat.report.page.problem.service.ProblemReportService;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager;
import com.dianping.cat.report.page.storage.service.LocalStorageService;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.report.page.state.service.LocalStateService;
import com.dianping.cat.report.page.top.service.LocalTopService;
import com.dianping.cat.report.page.transaction.service.LocalTransactionService;
import com.dianping.cat.report.LocalReportBucket;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketFactory;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.statistic.ServerStatisticManager;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.login.service.CookieManager;
import com.dianping.cat.system.page.login.service.TokenBuilder;
import com.dianping.cat.system.page.login.service.TokenManager;
import com.dianping.cat.system.page.permission.ResourceConfigManager;
import com.dianping.cat.system.page.permission.UserConfigManager;
import com.dianping.cat.system.page.router.config.RouterConfigManager;

@Configuration
@MapperScan(basePackages = {
		"com.dianping.cat.core.config.dao",
		"com.dianping.cat.core.report.daily.dao",
		"com.dianping.cat.core.mybatis.generated.hostinfo.dao",
		"com.dianping.cat.core.mybatis.generated.hourlyreport.dao",
		"com.dianping.cat.core.mybatis.generated.weeklyreport.dao",
		"com.dianping.cat.core.mybatis.generated.monthreport.dao",
		"com.dianping.cat.core.mybatis.generated.project.dao",
		"com.dianping.cat.core.mybatis.generated.daily.report.content.dao",
		"com.dianping.cat.core.mybatis.generated.hourly.report.content.dao",
		"com.dianping.cat.core.mybatis.generated.weekly.report.content.dao",
		"com.dianping.cat.core.mybatis.generated.monthly.report.content.dao",
		"com.dianping.cat.core.mybatis.generated.business.config.dao",
		"com.dianping.cat.core.mybatis.generated.task.dao",
		"com.dianping.cat.core.mybatis.generated.alert.summary.dao",
		"com.dianping.cat.core.mybatis.generated.config.modification.dao",
		"com.dianping.cat.core.mybatis.generated.baseline.dao",
		"com.dianping.cat.core.mybatis.generated.overload.dao",
		"com.dianping.cat.core.mybatis.generated.topologygraph.dao",
		"com.dianping.cat.core.mybatis.generated.metric.graph.dao",
		"com.dianping.cat.core.mybatis.generated.metric.screen.dao",
		"com.dianping.cat.core.mybatis.generated.alteration.dao",
		"com.dianping.cat.core.mybatis.generated.alert.dao",
		"com.dianping.cat.core.mybatis.generated.server.alarm.rule.dao",
		"com.dianping.cat.core.mybatis.generated.user.define.rule.dao"
})
public class CatHomeSpringConfiguration {
	@Bean
	public Logger plexusConsoleLogger() {
		return new ConsoleLogger(Logger.LEVEL_INFO, "spring-managed");
	}

	@Bean
	public ContentFetcher contentFetcher(Logger plexusConsoleLogger) {
		LocalResourceContentFetcher fetcher = new LocalResourceContentFetcher();

		fetcher.setLogger(plexusConsoleLogger.getChildLogger(LocalResourceContentFetcher.class.getName()));
		return fetcher;
	}

	@Bean
	public ConfigRepository configRepository(SqlSessionTemplate sqlSessionTemplate, TransactionTemplate transactionTemplate) {
		ConfigRepository repository = new ConfigRepository();

		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	@Bean(initMethod = "initialize")
	public ServerConfigManager serverConfigManager(ConfigRepository configRepository, ContentFetcher contentFetcher,
			Logger plexusConsoleLogger) {
		ServerConfigManager manager = new ServerConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		manager.enableLogging(plexusConsoleLogger.getChildLogger(ServerConfigManager.class.getName()));
		return manager;
	}

	@Bean
	public PathBuilder pathBuilder() {
		return new DefaultPathBuilder();
	}

	@Bean
	public MessageFinderManager messageFinderManager() {
		return new DefaultMessageFinderManager();
	}

	@Bean
	public ReportBucketFactory reportBucketFactory(PathBuilder pathBuilder, ServerConfigManager serverConfigManager) {
		return new ReportBucketFactory() {
			@Override
			public ReportBucket createReportBucket(String name, java.util.Date timestamp, int index)
			      throws java.io.IOException {
				LocalReportBucket bucket = new LocalReportBucket();

				bucket.setPathBuilder(pathBuilder);
				bucket.setConfigManager(serverConfigManager);
				bucket.initialize(name, timestamp, index);
				return bucket;
			}
		};
	}

	@Bean(initMethod = "initialize")
	public ReportBucketManager reportBucketManager(ServerConfigManager serverConfigManager,
			ReportBucketFactory reportBucketFactory) {
		DefaultReportBucketManager manager = new DefaultReportBucketManager();

		manager.setConfigManager(serverConfigManager);
		manager.setBucketFactory(reportBucketFactory);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public ServerFilterConfigManager serverFilterConfigManager(ConfigRepository configRepository,
			ContentFetcher contentFetcher) {
		ServerFilterConfigManager manager = new ServerFilterConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public SampleConfigManager sampleConfigManager(ConfigRepository configRepository, ContentFetcher contentFetcher) {
		SampleConfigManager manager = new SampleConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public ReportReloadConfigManager reportReloadConfigManager(ConfigRepository configRepository,
			ContentFetcher contentFetcher) {
		ReportReloadConfigManager manager = new ReportReloadConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public AtomicMessageConfigManager atomicMessageConfigManager(ConfigRepository configRepository,
			ContentFetcher contentFetcher) {
		AtomicMessageConfigManager manager = new AtomicMessageConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public TpValueStatisticConfigManager tpValueStatisticConfigManager(ConfigRepository configRepository,
			ContentFetcher contentFetcher, ServerConfigManager serverConfigManager) {
		TpValueStatisticConfigManager manager = new TpValueStatisticConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		manager.setServerConfigManager(serverConfigManager);
		return manager;
	}

	@Bean
	public BusinessConfigRepository businessConfigRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		BusinessConfigRepository repository = new BusinessConfigRepository();

		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	@Bean
	public ProjectRepository projectRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		ProjectRepository repository = new ProjectRepository();

		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	@Bean
	public HostinfoRepository hostinfoRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		HostinfoRepository repository = new HostinfoRepository();

		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	@Bean
	public DailyReportRepository dailyReportRepository() {
		return new DailyReportRepository();
	}

	@Bean
	public DailyReportContentRepository dailyReportContentRepository() {
		return new DailyReportContentRepository();
	}

	@Bean
	public HourlyReportRepository hourlyReportRepository() {
		return new HourlyReportRepository();
	}

	@Bean
	public HourlyReportContentRepository hourlyReportContentRepository() {
		return new HourlyReportContentRepository();
	}

	@Bean
	public WeeklyReportRepository weeklyReportRepository() {
		return new WeeklyReportRepository();
	}

	@Bean
	public WeeklyReportContentRepository weeklyReportContentRepository() {
		return new WeeklyReportContentRepository();
	}

	@Bean
	public MonthlyReportRepository monthlyReportRepository() {
		return new MonthlyReportRepository();
	}

	@Bean
	public MonthlyReportContentRepository monthlyReportContentRepository() {
		return new MonthlyReportContentRepository();
	}

	@Bean
	public OverloadRepository overloadRepository() {
		return new OverloadRepository();
	}

	@Bean
	public AlertRepository alertRepository() {
		return new AlertRepository();
	}

	@Bean
	public AlterationRepository alterationRepository() {
		return new AlterationRepository();
	}

	@Bean
	public BaselineRepository baselineRepository() {
		return new BaselineRepository();
	}

	@Bean
	public TopologyGraphRepository topologyGraphRepository() {
		return new TopologyGraphRepository();
	}

	@Bean
	public TaskRepository taskRepository() {
		return new TaskRepository();
	}

	@Bean
	public AlertSummaryRepository alertSummaryRepository() {
		return new AlertSummaryRepository();
	}

	@Bean
	public ConfigModificationRepository configModificationRepository() {
		return new ConfigModificationRepository();
	}

	@Bean
	public MetricGraphRepository metricGraphRepository() {
		return new MetricGraphRepository();
	}

	@Bean
	public MetricScreenRepository metricScreenRepository() {
		return new MetricScreenRepository();
	}

	@Bean
	public ServerAlarmRuleRepository serverAlarmRuleRepository() {
		return new ServerAlarmRuleRepository();
	}

	@Bean
	public UserDefineRuleRepository userDefineRuleRepository() {
		return new UserDefineRuleRepository();
	}

	@Bean
	public AlertSummaryService alertSummaryService() {
		return new AlertSummaryService();
	}

	@Bean
	public AlarmManager alarmManager() {
		return new AlarmManager();
	}

	@Bean
	public ProblemReportService problemReportService() {
		return new ProblemReportService();
	}

	@Bean(initMethod = "initialize", name = "problem-historical")
	public ModelService<ProblemReport> historicalProblemService(ProblemReportService problemReportService,
			ServerConfigManager serverConfigManager) {
		HistoricalProblemService service = new HistoricalProblemService();

		service.setReportService(problemReportService);
		service.setConfigManager(serverConfigManager);
		return service;
	}

	@Bean(initMethod = "initialize", name = ProblemAnalyzer.ID)
	public ModelService<ProblemReport> problemModelService(
			@Qualifier("problem-historical") ModelService<ProblemReport> historicalProblemService,
			ServerConfigManager serverConfigManager, RemoteServersManager remoteServersManager) {
		CompositeProblemService service = new CompositeProblemService();
		List<ModelService<ProblemReport>> services = Collections.singletonList(historicalProblemService);

		service.setServices(services);
		service.setConfigManager(serverConfigManager);
		service.setServerManager(remoteServersManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<ProblemReport> localProblemService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager) {
		LocalProblemService service = new LocalProblemService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<EventReport> localEventService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager) {
		LocalEventService service = new LocalEventService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<TransactionReport> localTransactionService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager) {
		LocalTransactionService service = new LocalTransactionService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<HeartbeatReport> localHeartbeatService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager) {
		LocalHeartbeatService service = new LocalHeartbeatService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<CrossReport> localCrossService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager) {
		LocalCrossService service = new LocalCrossService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<MatrixReport> localMatrixService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager) {
		LocalMatrixService service = new LocalMatrixService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<DependencyReport> localDependencyService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager) {
		LocalDependencyService service = new LocalDependencyService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<TopReport> localTopService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager) {
		LocalTopService service = new LocalTopService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<StateReport> localStateService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager) {
		LocalStateService service = new LocalStateService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<StorageReport> localStorageService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager) {
		LocalStorageService service = new LocalStorageService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<BusinessReport> localBusinessService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager) {
		LocalBusinessService service = new LocalBusinessService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<String> localMessageService(ServerConfigManager serverConfigManager) {
		LocalMessageService service = new LocalMessageService();

		service.setConfigManager(serverConfigManager);
		return service;
	}

	@Bean
	public Map<String, LocalModelService> localModelServices(
			@Qualifier("localProblemService") LocalModelService<ProblemReport> localProblemService,
			@Qualifier("localEventService") LocalModelService<EventReport> localEventService,
			@Qualifier("localTransactionService") LocalModelService<TransactionReport> localTransactionService,
			@Qualifier("localHeartbeatService") LocalModelService<HeartbeatReport> localHeartbeatService,
			@Qualifier("localCrossService") LocalModelService<CrossReport> localCrossService,
			@Qualifier("localMatrixService") LocalModelService<MatrixReport> localMatrixService,
			@Qualifier("localDependencyService") LocalModelService<DependencyReport> localDependencyService,
			@Qualifier("localTopService") LocalModelService<TopReport> localTopService,
			@Qualifier("localStateService") LocalModelService<StateReport> localStateService,
			@Qualifier("localStorageService") LocalModelService<StorageReport> localStorageService,
			@Qualifier("localBusinessService") LocalModelService<BusinessReport> localBusinessService,
			@Qualifier("localMessageService") LocalModelService<String> localMessageService) {
		Map<String, LocalModelService> services = new LinkedHashMap<String, LocalModelService>();

		services.put(LocalProblemService.ID, localProblemService);
		services.put(LocalEventService.ID, localEventService);
		services.put(LocalTransactionService.ID, localTransactionService);
		services.put(LocalHeartbeatService.ID, localHeartbeatService);
		services.put(LocalCrossService.ID, localCrossService);
		services.put(LocalMatrixService.ID, localMatrixService);
		services.put(LocalDependencyService.ID, localDependencyService);
		services.put(LocalTopService.ID, localTopService);
		services.put(LocalStateService.ID, localStateService);
		services.put(LocalStorageService.ID, localStorageService);
		services.put(LocalBusinessService.ID, localBusinessService);
		services.put("logview", localMessageService);
		return services;
	}

	@Bean
	public AlertInfoBuilder alertInfoBuilder(AlertRepository alertRepository) {
		AlertInfoBuilder builder = new AlertInfoBuilder();

		builder.setAlertDao(alertRepository);
		return builder;
	}

	@Bean(initMethod = "initialize", name = RelatedSummaryBuilder.ID)
	public SummaryBuilder relatedSummaryBuilder(AlertInfoBuilder alertInfoBuilder,
			AlertSummaryService alertSummaryService) {
		RelatedSummaryBuilder builder = new RelatedSummaryBuilder();

		builder.setAlertSummaryManager(alertInfoBuilder);
		builder.setAlertSummaryService(alertSummaryService);
		return builder;
	}

	@Bean(initMethod = "initialize", name = FailureSummaryBuilder.ID)
	public SummaryBuilder failureSummaryBuilder(@Qualifier(ProblemAnalyzer.ID) ModelService<ProblemReport> problemModelService) {
		FailureSummaryBuilder builder = new FailureSummaryBuilder();

		builder.setService(problemModelService);
		return builder;
	}

	@Bean(initMethod = "initialize", name = AlterationSummaryBuilder.ID)
	public SummaryBuilder alterationSummaryBuilder(AlterationRepository alterationRepository) {
		AlterationSummaryBuilder builder = new AlterationSummaryBuilder();

		builder.setAlterationDao(alterationRepository);
		return builder;
	}

	@Bean
	public AlertSummaryExecutor alertSummaryExecutor(@Qualifier(RelatedSummaryBuilder.ID) SummaryBuilder relatedBuilder,
			@Qualifier(FailureSummaryBuilder.ID) SummaryBuilder failureBuilder,
			@Qualifier(AlterationSummaryBuilder.ID) SummaryBuilder alterationBuilder,
			SenderManager senderManager) {
		AlertSummaryExecutor executor = new AlertSummaryExecutor();

		executor.setRelatedBuilder(relatedBuilder);
		executor.setFailureBuilder(failureBuilder);
		executor.setAlterationBuilder(alterationBuilder);
		executor.setSendManager(senderManager);
		return executor;
	}

	@Bean
	public UserDefinedRuleManager userDefinedRuleManager() {
		return new UserDefinedRuleManager();
	}

	@Bean
	public BaselineService baselineService() {
		return new DefaultBaselineService();
	}

	@Bean
	public RemoteServersManager remoteServersManager() {
		return new RemoteServersManager();
	}

	@Bean
	public DomainValidator domainValidator() {
		return new DomainValidator();
	}

	@Bean
	public ServerStatisticManager serverStatisticManager() {
		return new ServerStatisticManager();
	}

	@Bean
	public ConfigHtmlParser configHtmlParser() {
		return new ConfigHtmlParser();
	}

	@Bean(initMethod = "initialize")
	public RouterConfigManager routerConfigManager(ConfigRepository configRepository, ContentFetcher contentFetcher,
			DailyReportRepository dailyReportRepository, DailyReportContentRepository dailyReportContentRepository,
			Logger plexusConsoleLogger) {
		RouterConfigManager manager = new RouterConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		manager.setDailyReportDao(dailyReportRepository);
		manager.setDailyReportContentDao(dailyReportContentRepository);
		manager.enableLogging(plexusConsoleLogger.getChildLogger(RouterConfigManager.class.getName()));
		return manager;
	}

	@Bean(initMethod = "initialize")
	public DomainGroupConfigManager domainGroupConfigManager(ConfigRepository configRepository,
			ContentFetcher contentFetcher) {
		DomainGroupConfigManager manager = new DomainGroupConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public StorageGroupConfigManager storageGroupConfigManager(ConfigRepository configRepository,
			ContentFetcher contentFetcher) {
		StorageGroupConfigManager manager = new StorageGroupConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public TopologyGraphConfigManager topologyGraphConfigManager(ConfigRepository configRepository,
			ContentFetcher contentFetcher) {
		TopologyGraphConfigManager manager = new TopologyGraphConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public TopoGraphFormatConfigManager topoGraphFormatConfigManager(ConfigRepository configRepository,
			ContentFetcher contentFetcher) {
		TopoGraphFormatConfigManager manager = new TopoGraphFormatConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public HeartbeatDisplayPolicyManager heartbeatDisplayPolicyManager(ConfigRepository configRepository,
			ContentFetcher contentFetcher) {
		HeartbeatDisplayPolicyManager manager = new HeartbeatDisplayPolicyManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public ExceptionRuleConfigManager exceptionRuleConfigManager(ConfigRepository configRepository,
			ContentFetcher contentFetcher) {
		ExceptionRuleConfigManager manager = new ExceptionRuleConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public AlertConfigManager alertConfigManager(ConfigRepository configRepository, ContentFetcher contentFetcher) {
		AlertConfigManager manager = new AlertConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public AlertPolicyManager alertPolicyManager(ConfigRepository configRepository, ContentFetcher contentFetcher) {
		AlertPolicyManager manager = new AlertPolicyManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean
	public BaseRuleHelper baseRuleHelper() {
		return new BaseRuleHelper();
	}

	@Bean(initMethod = "initialize")
	public TransactionRuleConfigManager transactionRuleConfigManager(ConfigRepository configRepository,
			ContentFetcher contentFetcher, UserDefinedRuleManager userDefinedRuleManager, BaseRuleHelper baseRuleHelper) {
		TransactionRuleConfigManager manager = new TransactionRuleConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		manager.setUserDefinedRuleManager(userDefinedRuleManager);
		manager.setHelper(baseRuleHelper);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public EventRuleConfigManager eventRuleConfigManager(ConfigRepository configRepository, ContentFetcher contentFetcher,
			UserDefinedRuleManager userDefinedRuleManager, BaseRuleHelper baseRuleHelper) {
		EventRuleConfigManager manager = new EventRuleConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		manager.setUserDefinedRuleManager(userDefinedRuleManager);
		manager.setHelper(baseRuleHelper);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public HeartbeatRuleConfigManager heartbeatRuleConfigManager(ConfigRepository configRepository,
			ContentFetcher contentFetcher, UserDefinedRuleManager userDefinedRuleManager, BaseRuleHelper baseRuleHelper) {
		HeartbeatRuleConfigManager manager = new HeartbeatRuleConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		manager.setUserDefinedRuleManager(userDefinedRuleManager);
		manager.setHelper(baseRuleHelper);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public BusinessRuleConfigManager businessRuleConfigManager(BusinessConfigRepository businessConfigRepository) {
		BusinessRuleConfigManager manager = new BusinessRuleConfigManager();

		manager.setConfigDao(businessConfigRepository);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public BusinessTagConfigManager businessTagConfigManager(BusinessConfigRepository businessConfigRepository) {
		BusinessTagConfigManager manager = new BusinessTagConfigManager();

		manager.setConfigDao(businessConfigRepository);
		return manager;
	}

	@Bean
	public BusinessKeyHelper businessKeyHelper() {
		return new BusinessKeyHelper();
	}

	@Bean
	public BusinessDataFetcher businessDataFetcher(BusinessKeyHelper businessKeyHelper) {
		BusinessDataFetcher fetcher = new BusinessDataFetcher();

		fetcher.setKeyHelper(businessKeyHelper);
		return fetcher;
	}

	@Bean
	public CustomDataCalculator customDataCalculator(BusinessKeyHelper businessKeyHelper) {
		CustomDataCalculator calculator = new CustomDataCalculator();

		calculator.setKeyHelper(businessKeyHelper);
		return calculator;
	}

	@Bean
	public BusinessPointParser businessPointParser() {
		return new BusinessPointParser();
	}

	@Bean
	public BaselineConfigManager baselineConfigManager() {
		return new BaselineConfigManager();
	}

	@Bean
	public BaselineCreator baselineCreator() {
		return new DefaultBaselineCreator();
	}

	@Bean
	public BusinessReportService businessReportService() {
		return new BusinessReportService();
	}

	@Bean(initMethod = "initialize")
	public SenderConfigManager senderConfigManager(ConfigRepository configRepository, ContentFetcher contentFetcher) {
		SenderConfigManager manager = new SenderConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean
	public Sender mailSender(SenderConfigManager senderConfigManager) {
		MailSender sender = new MailSender();

		sender.setSenderConfigManager(senderConfigManager);
		return sender;
	}

	@Bean
	public Sender smsSender(SenderConfigManager senderConfigManager) {
		SmsSender sender = new SmsSender();

		sender.setSenderConfigManager(senderConfigManager);
		return sender;
	}

	@Bean
	public Sender weixinSender(SenderConfigManager senderConfigManager) {
		WeixinSender sender = new WeixinSender();

		sender.setSenderConfigManager(senderConfigManager);
		return sender;
	}

	@Bean
	public Map<String, Sender> alertSenders(@Qualifier("mailSender") Sender mailSender,
			@Qualifier("smsSender") Sender smsSender, @Qualifier("weixinSender") Sender weixinSender) {
		Map<String, Sender> senders = new LinkedHashMap<String, Sender>();

		senders.put(MailSender.ID, mailSender);
		senders.put(SmsSender.ID, smsSender);
		senders.put(WeixinSender.ID, weixinSender);
		return senders;
	}

	@Bean(initMethod = "initialize")
	public SenderManager senderManager(ServerConfigManager serverConfigManager,
			@Qualifier("alertSenders") Map<String, Sender> alertSenders) {
		SenderManager manager = new SenderManager();

		manager.setConfigManager(serverConfigManager);
		manager.setSenders(alertSenders);
		return manager;
	}

	@Bean
	public Spliter mailSpliter() {
		return new MailSpliter();
	}

	@Bean
	public Spliter smsSpliter() {
		return new SmsSpliter();
	}

	@Bean
	public Spliter weixinSpliter() {
		return new WeixinSpliter();
	}

	@Bean
	public Spliter dxSpliter() {
		return new DXSpliter();
	}

	@Bean
	public Map<String, Spliter> alertSpliters(@Qualifier("mailSpliter") Spliter mailSpliter,
			@Qualifier("smsSpliter") Spliter smsSpliter, @Qualifier("weixinSpliter") Spliter weixinSpliter,
			@Qualifier("dxSpliter") Spliter dxSpliter) {
		Map<String, Spliter> spliters = new LinkedHashMap<String, Spliter>();

		spliters.put(MailSpliter.ID, mailSpliter);
		spliters.put(SmsSpliter.ID, smsSpliter);
		spliters.put(WeixinSpliter.ID, weixinSpliter);
		spliters.put(DXSpliter.ID, dxSpliter);
		return spliters;
	}

	@Bean(initMethod = "initialize")
	public ContactorManager contactorManager(@Qualifier("alertContactors") Map<String, Contactor> alertContactors) {
		ContactorManager manager = new ContactorManager();

		manager.setContactors(alertContactors);
		return manager;
	}

	@Bean
	public Map<String, Contactor> alertContactors(@Qualifier("eventContactor") Contactor eventContactor,
			@Qualifier("heartbeatContactor") Contactor heartbeatContactor,
			@Qualifier("transactionContactor") Contactor transactionContactor,
			@Qualifier("businessContactor") Contactor businessContactor,
			@Qualifier("exceptionContactor") Contactor exceptionContactor) {
		Map<String, Contactor> contactors = new LinkedHashMap<String, Contactor>();

		contactors.put(EventContactor.ID, eventContactor);
		contactors.put(HeartbeatContactor.ID, heartbeatContactor);
		contactors.put(TransactionContactor.ID, transactionContactor);
		contactors.put(BusinessContactor.ID, businessContactor);
		contactors.put(ExceptionContactor.ID, exceptionContactor);
		return contactors;
	}

	@Bean
	public Contactor businessContactor(ProjectService projectService, AlertConfigManager alertConfigManager) {
		BusinessContactor contactor = new BusinessContactor();

		configureProjectContactor(contactor, projectService, alertConfigManager);
		return contactor;
	}

	@Bean
	public Contactor eventContactor(ProjectService projectService, AlertConfigManager alertConfigManager) {
		EventContactor contactor = new EventContactor();

		configureProjectContactor(contactor, projectService, alertConfigManager);
		return contactor;
	}

	@Bean
	public Contactor exceptionContactor(ProjectService projectService, AlertConfigManager alertConfigManager) {
		ExceptionContactor contactor = new ExceptionContactor();

		configureProjectContactor(contactor, projectService, alertConfigManager);
		return contactor;
	}

	@Bean
	public Contactor heartbeatContactor(ProjectService projectService, AlertConfigManager alertConfigManager) {
		HeartbeatContactor contactor = new HeartbeatContactor();

		configureProjectContactor(contactor, projectService, alertConfigManager);
		return contactor;
	}

	@Bean
	public Contactor transactionContactor(ProjectService projectService, AlertConfigManager alertConfigManager) {
		TransactionContactor contactor = new TransactionContactor();

		configureProjectContactor(contactor, projectService, alertConfigManager);
		return contactor;
	}

	@Bean(initMethod = "initialize")
	public SpliterManager spliterManager(@Qualifier("alertSpliters") Map<String, Spliter> alertSpliters) {
		SpliterManager manager = new SpliterManager();

		manager.setSpliters(alertSpliters);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public DecoratorManager decoratorManager(@Qualifier("alertDecorators") Map<String, Decorator> alertDecorators) {
		DecoratorManager manager = new DecoratorManager();

		manager.setDecorators(alertDecorators);
		return manager;
	}

	@Bean
	public Map<String, Decorator> alertDecorators(@Qualifier("eventDecorator") Decorator eventDecorator,
			@Qualifier("heartbeatDecorator") Decorator heartbeatDecorator,
			@Qualifier("transactionDecorator") Decorator transactionDecorator,
			@Qualifier("businessDecorator") Decorator businessDecorator,
			@Qualifier("exceptionDecorator") Decorator exceptionDecorator) {
		Map<String, Decorator> decorators = new LinkedHashMap<String, Decorator>();

		decorators.put(EventDecorator.ID, eventDecorator);
		decorators.put(HeartbeatDecorator.ID, heartbeatDecorator);
		decorators.put(TransactionDecorator.ID, transactionDecorator);
		decorators.put(BusinessDecorator.ID, businessDecorator);
		decorators.put(ExceptionDecorator.ID, exceptionDecorator);
		return decorators;
	}

	@Bean
	public Decorator businessDecorator(ProjectService projectService, AlertSummaryExecutor alertSummaryExecutor) {
		BusinessDecorator decorator = new BusinessDecorator();

		decorator.setProjectService(projectService);
		decorator.setExecutor(alertSummaryExecutor);
		return decorator;
	}

	@Bean(initMethod = "initialize")
	public Decorator exceptionDecorator(ProjectService projectService, AlertSummaryExecutor alertSummaryExecutor) {
		ExceptionDecorator decorator = new ExceptionDecorator();

		decorator.setProjectService(projectService);
		decorator.setExecutor(alertSummaryExecutor);
		return decorator;
	}

	@Bean(initMethod = "initialize")
	public Decorator eventDecorator() {
		return new EventDecorator();
	}

	@Bean
	public Decorator heartbeatDecorator() {
		return new HeartbeatDecorator();
	}

	@Bean(initMethod = "initialize")
	public Decorator transactionDecorator() {
		return new TransactionDecorator();
	}

	@Bean(initMethod = "initialize")
	public UserConfigManager userConfigManager(ConfigRepository configRepository, ContentFetcher contentFetcher) {
		UserConfigManager manager = new UserConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public ResourceConfigManager resourceConfigManager(ConfigRepository configRepository, ContentFetcher contentFetcher) {
		ResourceConfigManager manager = new ResourceConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean
	public CookieManager cookieManager() {
		return new CookieManager();
	}

	@Bean
	public TokenBuilder tokenBuilder() {
		return new TokenBuilder();
	}

	@Bean
	public TokenManager tokenManager(CookieManager cookieManager, TokenBuilder tokenBuilder) {
		TokenManager manager = new TokenManager();

		manager.setCookieManager(cookieManager);
		manager.setTokenBuilder(tokenBuilder);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public ProjectService projectService(ProjectRepository projectRepository, ServerConfigManager serverConfigManager) {
		ProjectService service = new ProjectService();

		service.setProjectDao(projectRepository);
		service.setServerConfigManager(serverConfigManager);
		return service;
	}

	@Bean
	public HostinfoService hostinfoService(HostinfoRepository hostinfoRepository,
			ServerConfigManager serverConfigManager) {
		HostinfoService service = new HostinfoService();

		service.setHostinfoDao(hostinfoRepository);
		service.setServerConfigManager(serverConfigManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public BusinessConfigManager businessConfigManager(BusinessConfigRepository businessConfigRepository,
			ServerConfigManager serverConfigManager) {
		BusinessConfigManager manager = new BusinessConfigManager();

		manager.setConfigDao(businessConfigRepository);
		manager.setServerConfigManager(serverConfigManager);
		return manager;
	}

	@Bean
	public DataSource catDataSource() {
		return CatHomeSpringDataSourceFactory.createCatDataSource();
	}

	private void configureProjectContactor(ProjectContactor contactor, ProjectService projectService,
			AlertConfigManager alertConfigManager) {
		contactor.setProjectService(projectService);
		contactor.setConfigManager(alertConfigManager);
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource catDataSource) throws Exception {
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

		factory.setDataSource(catDataSource);
		factory.setMapperLocations(
				resolver.getResource("classpath:mybatis/mapper/ConfigMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/DailyReportMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/HostinfoMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/HourlyreportMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/WeeklyreportMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/MonthreportMapper.xml"),
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

	@Bean(initMethod = "verify")
	public CatHomeSpringStartupVerifier catHomeSpringStartupVerifier(SqlSessionTemplate sqlSessionTemplate) {
		return new CatHomeSpringStartupVerifier(sqlSessionTemplate);
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
