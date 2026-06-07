package com.dianping.cat.home.spring;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import com.dianping.cat.core.config.repository.ConfigRepository;
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
import com.dianping.cat.report.alert.spi.config.UserDefinedRuleManager;
import com.dianping.cat.report.alert.summary.AlertSummaryService;
import com.dianping.cat.report.DomainValidator;
import com.dianping.cat.report.page.metric.service.BaselineService;
import com.dianping.cat.report.page.metric.service.DefaultBaselineService;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.statistic.ServerStatisticManager;

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

	@Bean(initMethod = "initialize")
	public ProjectService projectService(ProjectRepository projectRepository, ServerConfigManager serverConfigManager) {
		ProjectService service = new ProjectService();

		service.setProjectDao(projectRepository);
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
