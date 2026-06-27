package com.dianping.cat.home.spring;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketFactory;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.cat.message.storage.BlockDumperFactory;
import org.unidal.cat.message.storage.BlockWriterFactory;
import org.unidal.cat.message.storage.IndexFactory;
import org.unidal.cat.message.storage.IndexManager;
import org.unidal.cat.message.storage.MessageDumperManager;
import org.unidal.cat.message.storage.MessageDumperFactory;
import org.unidal.cat.message.storage.MessageFinderManager;
import org.unidal.cat.message.storage.MessageProcessorFactory;
import org.unidal.cat.message.storage.StorageConfiguration;
import org.unidal.cat.message.storage.TokenMappingFactory;
import org.unidal.cat.message.storage.TokenMappingManager;
import org.unidal.cat.message.storage.internals.ByteBufCache;
import org.unidal.cat.message.storage.internals.DefaultBlockDumper;
import org.unidal.cat.message.storage.internals.DefaultBlockDumperManager;
import org.unidal.cat.message.storage.internals.DefaultBlockWriter;
import org.unidal.cat.message.storage.internals.DefaultMessageFinderManager;
import org.unidal.cat.message.storage.internals.DefaultMessageDumper;
import org.unidal.cat.message.storage.internals.DefaultMessageDumperManager;
import org.unidal.cat.message.storage.internals.DefaultMessageProcessor;
import org.unidal.cat.message.storage.internals.DefaultByteBufCache;
import org.unidal.cat.message.storage.internals.DefaultStorageConfiguration;
import org.unidal.cat.message.storage.hdfs.HdfsBucket;
import org.unidal.cat.message.storage.hdfs.HdfsBucketManager;
import org.unidal.cat.message.storage.hdfs.HdfsFileBuilder;
import org.unidal.cat.message.storage.hdfs.HdfsIndex;
import org.unidal.cat.message.storage.hdfs.HdfsIndexManager;
import org.unidal.cat.message.storage.hdfs.HdfsMessageConsumerFinder;
import org.unidal.cat.message.storage.hdfs.HdfsSystemManager;
import org.unidal.cat.message.storage.hdfs.HdfsTokenMapping;
import org.unidal.cat.message.storage.hdfs.HdfsTokenMappingManager;
import org.unidal.cat.message.storage.hdfs.MessageConsumerFinder;
import org.unidal.cat.message.storage.local.LocalBucket;
import org.unidal.cat.message.storage.local.LocalBucketManager;
import org.unidal.cat.message.storage.local.LocalFileBuilder;
import org.unidal.cat.message.storage.local.LocalIndex;
import org.unidal.cat.message.storage.local.LocalIndexManager;
import org.unidal.cat.message.storage.local.LocalTokenMapping;
import org.unidal.cat.message.storage.local.LocalTokenMappingManager;
import org.unidal.cat.message.storage.clean.HdfsUploader;
import org.unidal.cat.message.storage.clean.LogviewProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.analysis.ContainerMessageAnalyzerFactory;
import com.dianping.cat.analysis.DefaultMessageAnalyzerManager;
import com.dianping.cat.analysis.DefaultMessageHandler;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzerFactory;
import com.dianping.cat.analysis.MessageAnalyzerManager;
import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.analysis.MessageHandler;
import com.dianping.cat.analysis.RealtimeConsumer;
import com.dianping.cat.analysis.TcpSocketReceiver;
import com.dianping.cat.config.AtomicMessageConfigManager;
import com.dianping.cat.config.ReportReloadConfigManager;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.config.content.LocalResourceContentFetcher;
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.config.transaction.TpValueStatisticConfigManager;
import com.dianping.cat.consumer.DatabaseParser;
import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.BusinessDelegate;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.CrossDelegate;
import com.dianping.cat.consumer.cross.IpConvertManager;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyDelegate;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dump.DumpAnalyzer;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.EventDelegate;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatDelegate;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixDelegate;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.ProblemDelegate;
import com.dianping.cat.consumer.problem.DefaultProblemHandler;
import com.dianping.cat.consumer.problem.LongExecutionProblemHandler;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.StateDelegate;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.StorageDelegate;
import com.dianping.cat.consumer.storage.StorageReportUpdater;
import com.dianping.cat.consumer.storage.builder.StorageBuilderManager;
import com.dianping.cat.consumer.storage.builder.StorageCacheBuilder;
import com.dianping.cat.consumer.storage.builder.StorageRPCBuilder;
import com.dianping.cat.consumer.storage.builder.StorageSQLBuilder;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.TopDelegate;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.consumer.dump.LocalMessageBucketManager;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionDelegate;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.mybatis.ConfigRepository;
import com.dianping.cat.mybatis.SpringBackedRepositorySupport;
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
import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketFactory;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.hadoop.hdfs.bucket.AbstractHdfsMessageBucket;
import com.dianping.cat.hadoop.hdfs.bucket.HarfsMessageBucket;
import com.dianping.cat.hadoop.hdfs.bucket.HdfsMessageBucket;
import com.dianping.cat.home.spring.storage.SpringBackedBlockDumperManager;
import com.dianping.cat.home.spring.storage.SpringBackedMessageDumperManager;
import com.dianping.cat.message.DefaultPathBuilder;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.message.storage.LocalMessageBucket;
import com.dianping.cat.message.storage.MessageBucketFactory;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.mvc.ReportModelDependencies;
import com.dianping.cat.report.DefaultReportBucketManager;
import com.dianping.cat.report.DefaultReportManager;
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
import com.dianping.cat.report.service.AbstractReportService;
import com.dianping.cat.report.task.TaskBuilder;
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
import com.dianping.cat.report.LocalReportBucket;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketFactory;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.task.reload.AbstractReportReloader;
import com.dianping.cat.report.task.reload.ReportReloader;
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
@Import(SpringMvcMigrationConfiguration.class)
@ComponentScan(basePackageClasses = {BusinessAnalyzer.class, BusinessDelegate.class,
		TransactionAnalyzer.class, TransactionDelegate.class, CrossAnalyzer.class, CrossDelegate.class,
		DumpAnalyzer.class, DependencyAnalyzer.class, DependencyDelegate.class, EventAnalyzer.class, EventDelegate.class,
		HeartbeatAnalyzer.class, HeartbeatDelegate.class, MatrixAnalyzer.class, MatrixDelegate.class,
		ProblemAnalyzer.class, ProblemDelegate.class, StorageAnalyzer.class, StorageDelegate.class,
		StorageReportUpdater.class, StorageBuilderManager.class, TopAnalyzer.class, TopDelegate.class, StateAnalyzer.class, StateDelegate.class,
		ContainerMessageAnalyzerFactory.class, BusinessKeyHelper.class, BusinessDataFetcher.class,
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
		AllReportConfigManager.class, ServerFilterConfigManager.class, SampleConfigManager.class,
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
					ContainerMessageAnalyzerFactory.class, BusinessKeyHelper.class, BusinessDataFetcher.class,
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
					AllReportConfigManager.class, ServerFilterConfigManager.class, SampleConfigManager.class,
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
	@Bean(initMethod = "initialize")
	public MessageAnalyzerManager messageAnalyzerManager(MessageAnalyzerFactory messageAnalyzerFactory,
			ServerConfigManager serverConfigManager) {
		DefaultMessageAnalyzerManager manager = new DefaultMessageAnalyzerManager();

		manager.setAnalyzerFactory(messageAnalyzerFactory);
		manager.setConfigManager(serverConfigManager);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public MessageConsumer messageConsumer(MessageAnalyzerManager messageAnalyzerManager,
			ServerStatisticManager serverStatisticManager) {
		RealtimeConsumer consumer = new RealtimeConsumer();

		consumer.setAnalyzerManager(messageAnalyzerManager);
		consumer.setServerStateManager(serverStatisticManager);
		return consumer;
	}

	@Bean
	public MessageHandler messageHandler(MessageConsumer messageConsumer) {
		DefaultMessageHandler handler = new DefaultMessageHandler();

		handler.setConsumer(messageConsumer);
		return handler;
	}

	@Bean
	public TcpSocketReceiver tcpSocketReceiver(ServerConfigManager serverConfigManager, MessageHandler messageHandler,
			ServerStatisticManager serverStatisticManager) {
		TcpSocketReceiver receiver = new TcpSocketReceiver();

		receiver.setServerConfigManager(serverConfigManager);
		receiver.setHandler(messageHandler);
		receiver.setServerStateManager(serverStatisticManager);
		return receiver;
	}

	@Bean(initMethod = "initialize")
	public HdfsSystemManager hdfsSystemManager(ServerConfigManager serverConfigManager) {
		HdfsSystemManager manager = new HdfsSystemManager();

		manager.setConfigManager(serverConfigManager);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public HdfsUploader hdfsUploader(HdfsSystemManager hdfsSystemManager, ServerConfigManager serverConfigManager) {
		HdfsUploader uploader = new HdfsUploader();

		uploader.setFileSystemManager(hdfsSystemManager);
		uploader.setServerConfigManager(serverConfigManager);
		return uploader;
	}

	@Bean(initMethod = "initialize")
	public LogviewProcessor logviewProcessor(HdfsUploader hdfsUploader, ServerConfigManager serverConfigManager) {
		LogviewProcessor processor = new LogviewProcessor();

		processor.setHdfsUploader(hdfsUploader);
		processor.setConfigManager(serverConfigManager);
		return processor;
	}

	@Bean(initMethod = "start", destroyMethod = "shutdown")
	public CatHomeRuntimeBootstrap catHomeRuntimeBootstrap(AlarmManager alarmManager,
			DefaultTaskConsumer defaultTaskConsumer, LogviewProcessor logviewProcessor, MessageConsumer messageConsumer,
			ReportReloadTask reportReloadTask, ServerConfigManager serverConfigManager,
			ServersUpdaterManager serversUpdaterManager, TcpSocketReceiver tcpSocketReceiver) {
		CatHomeRuntimeBootstrap bootstrap = new CatHomeRuntimeBootstrap();

		bootstrap.setAlarmManager(alarmManager);
		bootstrap.setTaskConsumer(defaultTaskConsumer);
		bootstrap.setLogviewProcessor(logviewProcessor);
		bootstrap.setMessageConsumer(messageConsumer);
		bootstrap.setReportReloadTask(reportReloadTask);
		bootstrap.setServerConfigManager(serverConfigManager);
		bootstrap.setServersUpdaterManager(serversUpdaterManager);
		bootstrap.setTcpSocketReceiver(tcpSocketReceiver);
		return bootstrap;
	}

	@Bean(name = BusinessAnalyzer.ID + "ReportManager", initMethod = "initialize")
	@Scope("prototype")
	public ReportManager<BusinessReport> businessReportManager(
			@Qualifier("businessDelegate") ReportDelegate<BusinessReport> businessDelegate,
			ReportBucketManager reportBucketManager, HourlyReportRepository hourlyReportRepository,
			HourlyReportContentRepository hourlyReportContentRepository, DomainValidator domainValidator) {
		DefaultReportManager<BusinessReport> manager = new DefaultReportManager<BusinessReport>();

		manager.setReportDelegate(businessDelegate);
		manager.setBucketManager(reportBucketManager);
		manager.setReportDao(hourlyReportRepository);
		manager.setReportContentDao(hourlyReportContentRepository);
		manager.setValidator(domainValidator);
		manager.setName(BusinessAnalyzer.ID);
		return manager;
	}

	@Bean(name = TransactionAnalyzer.ID + "ReportManager", initMethod = "initialize")
	@Scope("prototype")
	public ReportManager<TransactionReport> transactionReportManager(
			@Qualifier("transactionDelegate") ReportDelegate<TransactionReport> transactionDelegate,
			ReportBucketManager reportBucketManager, HourlyReportRepository hourlyReportRepository,
			HourlyReportContentRepository hourlyReportContentRepository, DomainValidator domainValidator) {
		DefaultReportManager<TransactionReport> manager = new DefaultReportManager<TransactionReport>();

		manager.setReportDelegate(transactionDelegate);
		manager.setBucketManager(reportBucketManager);
		manager.setReportDao(hourlyReportRepository);
		manager.setReportContentDao(hourlyReportContentRepository);
		manager.setValidator(domainValidator);
		manager.setName(TransactionAnalyzer.ID);
		return manager;
	}

	@Bean(name = CrossAnalyzer.ID + "ReportManager", initMethod = "initialize")
	@Scope("prototype")
	public ReportManager<CrossReport> crossReportManager(
			@Qualifier("crossDelegate") ReportDelegate<CrossReport> crossDelegate,
			ReportBucketManager reportBucketManager, HourlyReportRepository hourlyReportRepository,
			HourlyReportContentRepository hourlyReportContentRepository, DomainValidator domainValidator) {
		DefaultReportManager<CrossReport> manager = new DefaultReportManager<CrossReport>();

		manager.setReportDelegate(crossDelegate);
		manager.setBucketManager(reportBucketManager);
		manager.setReportDao(hourlyReportRepository);
		manager.setReportContentDao(hourlyReportContentRepository);
		manager.setValidator(domainValidator);
		manager.setName(CrossAnalyzer.ID);
		return manager;
	}

	@Bean(name = DependencyAnalyzer.ID + "ReportManager", initMethod = "initialize")
	@Scope("prototype")
	public ReportManager<DependencyReport> dependencyReportManager(
			@Qualifier("dependencyDelegate") ReportDelegate<DependencyReport> dependencyDelegate,
			ReportBucketManager reportBucketManager, HourlyReportRepository hourlyReportRepository,
			HourlyReportContentRepository hourlyReportContentRepository, DomainValidator domainValidator) {
		DefaultReportManager<DependencyReport> manager = new DefaultReportManager<DependencyReport>();

		manager.setReportDelegate(dependencyDelegate);
		manager.setBucketManager(reportBucketManager);
		manager.setReportDao(hourlyReportRepository);
		manager.setReportContentDao(hourlyReportContentRepository);
		manager.setValidator(domainValidator);
		manager.setName(DependencyAnalyzer.ID);
		return manager;
	}

	@Bean(name = EventAnalyzer.ID + "ReportManager", initMethod = "initialize")
	@Scope("prototype")
	public ReportManager<EventReport> eventReportManager(
			@Qualifier("eventDelegate") ReportDelegate<EventReport> eventDelegate,
			ReportBucketManager reportBucketManager, HourlyReportRepository hourlyReportRepository,
			HourlyReportContentRepository hourlyReportContentRepository, DomainValidator domainValidator) {
		DefaultReportManager<EventReport> manager = new DefaultReportManager<EventReport>();

		manager.setReportDelegate(eventDelegate);
		manager.setBucketManager(reportBucketManager);
		manager.setReportDao(hourlyReportRepository);
		manager.setReportContentDao(hourlyReportContentRepository);
		manager.setValidator(domainValidator);
		manager.setName(EventAnalyzer.ID);
		return manager;
	}

	@Bean(name = HeartbeatAnalyzer.ID + "ReportManager", initMethod = "initialize")
	@Scope("prototype")
	public ReportManager<HeartbeatReport> heartbeatReportManager(
			@Qualifier("heartbeatDelegate") ReportDelegate<HeartbeatReport> heartbeatDelegate,
			ReportBucketManager reportBucketManager, HourlyReportRepository hourlyReportRepository,
			HourlyReportContentRepository hourlyReportContentRepository, DomainValidator domainValidator) {
		DefaultReportManager<HeartbeatReport> manager = new DefaultReportManager<HeartbeatReport>();

		manager.setReportDelegate(heartbeatDelegate);
		manager.setBucketManager(reportBucketManager);
		manager.setReportDao(hourlyReportRepository);
		manager.setReportContentDao(hourlyReportContentRepository);
		manager.setValidator(domainValidator);
		manager.setName(HeartbeatAnalyzer.ID);
		return manager;
	}

	@Bean(name = MatrixAnalyzer.ID + "ReportManager", initMethod = "initialize")
	@Scope("prototype")
	public ReportManager<MatrixReport> matrixReportManager(
			@Qualifier("matrixDelegate") ReportDelegate<MatrixReport> matrixDelegate,
			ReportBucketManager reportBucketManager, HourlyReportRepository hourlyReportRepository,
			HourlyReportContentRepository hourlyReportContentRepository, DomainValidator domainValidator) {
		DefaultReportManager<MatrixReport> manager = new DefaultReportManager<MatrixReport>();

		manager.setReportDelegate(matrixDelegate);
		manager.setBucketManager(reportBucketManager);
		manager.setReportDao(hourlyReportRepository);
		manager.setReportContentDao(hourlyReportContentRepository);
		manager.setValidator(domainValidator);
		manager.setName(MatrixAnalyzer.ID);
		return manager;
	}

	@Bean(name = ProblemAnalyzer.ID + "ReportManager", initMethod = "initialize")
	@Scope("prototype")
	public ReportManager<ProblemReport> problemReportManager(
			@Qualifier("problemDelegate") ReportDelegate<ProblemReport> problemDelegate,
			ReportBucketManager reportBucketManager, HourlyReportRepository hourlyReportRepository,
			HourlyReportContentRepository hourlyReportContentRepository, DomainValidator domainValidator) {
		DefaultReportManager<ProblemReport> manager = new DefaultReportManager<ProblemReport>();

		manager.setReportDelegate(problemDelegate);
		manager.setBucketManager(reportBucketManager);
		manager.setReportDao(hourlyReportRepository);
		manager.setReportContentDao(hourlyReportContentRepository);
		manager.setValidator(domainValidator);
		manager.setName(ProblemAnalyzer.ID);
		return manager;
	}

	@Bean(name = StorageAnalyzer.ID + "ReportManager", initMethod = "initialize")
	@Scope("prototype")
	public ReportManager<StorageReport> storageReportManager(
			@Qualifier("storageDelegate") ReportDelegate<StorageReport> storageDelegate,
			ReportBucketManager reportBucketManager, HourlyReportRepository hourlyReportRepository,
			HourlyReportContentRepository hourlyReportContentRepository, DomainValidator domainValidator) {
		DefaultReportManager<StorageReport> manager = new DefaultReportManager<StorageReport>();

		manager.setReportDelegate(storageDelegate);
		manager.setBucketManager(reportBucketManager);
		manager.setReportDao(hourlyReportRepository);
		manager.setReportContentDao(hourlyReportContentRepository);
		manager.setValidator(domainValidator);
		manager.setName(StorageAnalyzer.ID);
		return manager;
	}

	@Bean(name = TopAnalyzer.ID + "ReportManager", initMethod = "initialize")
	@Scope("prototype")
	public ReportManager<TopReport> topReportManager(@Qualifier("topDelegate") ReportDelegate<TopReport> topDelegate,
			ReportBucketManager reportBucketManager, HourlyReportRepository hourlyReportRepository,
			HourlyReportContentRepository hourlyReportContentRepository, DomainValidator domainValidator) {
		DefaultReportManager<TopReport> manager = new DefaultReportManager<TopReport>();

		manager.setReportDelegate(topDelegate);
		manager.setBucketManager(reportBucketManager);
		manager.setReportDao(hourlyReportRepository);
		manager.setReportContentDao(hourlyReportContentRepository);
		manager.setValidator(domainValidator);
		manager.setName(TopAnalyzer.ID);
		return manager;
	}

	@Bean(name = StateAnalyzer.ID + "ReportManager", initMethod = "initialize")
	@Scope("prototype")
	public ReportManager<StateReport> stateReportManager(
			@Qualifier("stateDelegate") ReportDelegate<StateReport> stateDelegate,
			ReportBucketManager reportBucketManager, HourlyReportRepository hourlyReportRepository,
			HourlyReportContentRepository hourlyReportContentRepository, DomainValidator domainValidator) {
		DefaultReportManager<StateReport> manager = new DefaultReportManager<StateReport>();

		manager.setReportDelegate(stateDelegate);
		manager.setBucketManager(reportBucketManager);
		manager.setReportDao(hourlyReportRepository);
		manager.setReportContentDao(hourlyReportContentRepository);
		manager.setValidator(domainValidator);
		manager.setName(StateAnalyzer.ID);
		return manager;
	}

	@Bean
	public Map<String, ReportReloader> reportReloaders(@Qualifier("businessReportReloader") ReportReloader businessReportReloader,
			@Qualifier("transactionReportReloader") ReportReloader transactionReportReloader,
			@Qualifier("crossReportReloader") ReportReloader crossReportReloader,
			@Qualifier("dependencyReportReloader") ReportReloader dependencyReportReloader,
			@Qualifier("eventReportReloader") ReportReloader eventReportReloader,
			@Qualifier("heartbeatReportReloader") ReportReloader heartbeatReportReloader,
			@Qualifier("matrixReportReloader") ReportReloader matrixReportReloader,
			@Qualifier("problemReportReloader") ReportReloader problemReportReloader,
			@Qualifier("storageReportReloader") ReportReloader storageReportReloader,
			@Qualifier("topReportReloader") ReportReloader topReportReloader,
			@Qualifier("stateReportReloader") ReportReloader stateReportReloader) {
		Map<String, ReportReloader> reloaders = new LinkedHashMap<String, ReportReloader>();

		reloaders.put(businessReportReloader.getId(), businessReportReloader);
		reloaders.put(transactionReportReloader.getId(), transactionReportReloader);
		reloaders.put(crossReportReloader.getId(), crossReportReloader);
		reloaders.put(dependencyReportReloader.getId(), dependencyReportReloader);
		reloaders.put(eventReportReloader.getId(), eventReportReloader);
		reloaders.put(heartbeatReportReloader.getId(), heartbeatReportReloader);
		reloaders.put(matrixReportReloader.getId(), matrixReportReloader);
		reloaders.put(problemReportReloader.getId(), problemReportReloader);
		reloaders.put(storageReportReloader.getId(), storageReportReloader);
		reloaders.put(topReportReloader.getId(), topReportReloader);
		reloaders.put(stateReportReloader.getId(), stateReportReloader);
		return reloaders;
	}

	@Bean
	public Map<String, TaskBuilder> taskBuilders(Map<String, TaskBuilder> taskBuilders) {
		return taskBuilders;
	}

	@Bean
	public ConfigRepository configRepository(SqlSessionTemplate sqlSessionTemplate, TransactionTemplate transactionTemplate) {
		ConfigRepository repository = new ConfigRepository();

		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	@Bean(initMethod = "initialize")
	public ServerConfigManager serverConfigManager(ConfigRepository configRepository, ContentFetcher contentFetcher) {
		ServerConfigManager manager = new ServerConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean
	public MessageBucketFactory legacyMessageBucketFactory() {
		return (baseDir, dataFile) -> {
			LocalMessageBucket bucket = new LocalMessageBucket();

			bucket.setBaseDir(baseDir);
			bucket.initialize(dataFile);
			return bucket;
		};
	}

	@Bean(initMethod = "initialize", name = "legacyLocalMessageBucketManager")
	public MessageBucketManager localMessageBucketManager(ServerConfigManager serverConfigManager, PathBuilder pathBuilder,
			ServerStatisticManager serverStatisticManager, MessageBucketFactory legacyMessageBucketFactory) {
		LocalMessageBucketManager manager = new LocalMessageBucketManager();

		manager.setConfigManager(serverConfigManager);
		manager.setPathBuilder(pathBuilder);
		manager.setServerStateManager(serverStatisticManager);
		manager.setBucketFactory(legacyMessageBucketFactory);
		return manager;
	}

	@Bean
	public MessageFinderManager messageFinderManager() {
		return new DefaultMessageFinderManager();
	}

	@Bean
	@Primary
	public MessageDumperManager messageDumperManager(BlockDumperManager blockDumperManager,
			BucketManager localBucketManager, MessageFinderManager messageFinderManager,
			ServerConfigManager serverConfigManager, ServerStatisticManager serverStatisticManager) {
		SpringBackedMessageDumperManager manager = new SpringBackedMessageDumperManager();

		manager.setBlockDumperManager(blockDumperManager);
		manager.setBucketManager(localBucketManager);
		manager.setConfigManager(serverConfigManager);
		manager.setFinderManager(messageFinderManager);
		manager.setStatisticManager(serverStatisticManager);
		return manager;
	}

	@Bean
	public MessageProcessorFactory legacyMessageProcessorFactory(BlockDumperManager blockDumperManager,
			MessageFinderManager messageFinderManager, ServerConfigManager serverConfigManager) {
		return (hour, index, queue) -> {
			DefaultMessageProcessor processor = new DefaultMessageProcessor();

			processor.setBlockDumperManager(blockDumperManager);
			processor.setFinderManager(messageFinderManager);
			processor.setConfigManager(serverConfigManager);
			processor.initialize(hour, index, queue);
			return processor;
		};
	}

	@Bean
	public MessageDumperFactory legacyMessageDumperFactory(BlockDumperManager blockDumperManager,
			BucketManager localBucketManager, MessageProcessorFactory legacyMessageProcessorFactory,
			ServerConfigManager serverConfigManager, ServerStatisticManager serverStatisticManager) {
		return hour -> {
			DefaultMessageDumper dumper = new DefaultMessageDumper();

			dumper.setBlockDumperManager(blockDumperManager);
			dumper.setBucketManager(localBucketManager);
			dumper.setConfigManager(serverConfigManager);
			dumper.setMessageProcessorFactory(legacyMessageProcessorFactory);
			dumper.setStatisticManager(serverStatisticManager);
			dumper.initialize(hour);
			return dumper;
		};
	}

	@Bean(initMethod = "initialize", name = "legacyMessageDumperManager")
	public MessageDumperManager legacyMessageDumperManager(MessageDumperFactory legacyMessageDumperFactory) {
		DefaultMessageDumperManager manager = new DefaultMessageDumperManager();

		manager.setMessageDumperFactory(legacyMessageDumperFactory);
		return manager;
	}

	@Bean
	@Primary
	public BlockDumperManager blockDumperManager(BucketManager localBucketManager,
			ServerConfigManager serverConfigManager, ServerStatisticManager serverStatisticManager) {
		SpringBackedBlockDumperManager manager = new SpringBackedBlockDumperManager();

		manager.setBucketManager(localBucketManager);
		manager.setConfigManager(serverConfigManager);
		manager.setStatisticManager(serverStatisticManager);
		return manager;
	}

	@Bean
	public BlockWriterFactory legacyBlockWriterFactory(BucketManager localBucketManager,
			ServerStatisticManager serverStatisticManager) {
		return (hour, index, queue) -> {
			DefaultBlockWriter writer = new DefaultBlockWriter();

			writer.setBucketManager(localBucketManager);
			writer.setStatisticManager(serverStatisticManager);
			writer.initialize(hour, index, queue);
			return writer;
		};
	}

	@Bean
	public BlockDumperFactory legacyBlockDumperFactory(BlockWriterFactory legacyBlockWriterFactory,
			ServerConfigManager serverConfigManager, ServerStatisticManager serverStatisticManager) {
		return hour -> {
			DefaultBlockDumper dumper = new DefaultBlockDumper();

			dumper.setBlockWriterFactory(legacyBlockWriterFactory);
			dumper.setConfigManager(serverConfigManager);
			dumper.setStatisticManager(serverStatisticManager);
			dumper.initialize(hour);
			return dumper;
		};
	}

	@Bean(name = "legacyBlockDumperManager")
	public BlockDumperManager legacyBlockDumperManager(BlockDumperFactory legacyBlockDumperFactory) {
		DefaultBlockDumperManager manager = new DefaultBlockDumperManager();

		manager.setBlockDumperFactory(legacyBlockDumperFactory);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public StorageConfiguration storageConfiguration() {
		return new DefaultStorageConfiguration();
	}

	@Bean
	public org.unidal.cat.message.storage.PathBuilder localMessagePathBuilder(StorageConfiguration storageConfiguration) {
		LocalFileBuilder builder = new LocalFileBuilder();

		builder.setConfig(storageConfiguration);
		return builder;
	}

	@Bean
	public org.unidal.cat.message.storage.PathBuilder hdfsMessagePathBuilder(HdfsSystemManager hdfsSystemManager) {
		HdfsFileBuilder builder = new HdfsFileBuilder();

		builder.setFileSystemManager(hdfsSystemManager);
		return builder;
	}

	@Bean(initMethod = "initialize")
	public ByteBufCache byteBufCache() {
		DefaultByteBufCache cache = new DefaultByteBufCache();

		return cache;
	}

	@Bean
	public BucketFactory localMessageBucketFactory(
			@Qualifier("localMessagePathBuilder") org.unidal.cat.message.storage.PathBuilder localMessagePathBuilder,
			ByteBufCache byteBufCache, ServerConfigManager serverConfigManager) {
		return new BucketFactory() {
			@Override
			public Bucket createBucket(String domain, String ip, int hour, boolean writeMode) {
				LocalBucket bucket = new LocalBucket();

				bucket.setPathBuilder(localMessagePathBuilder);
				bucket.setBufCache(byteBufCache);
				bucket.setConfig(serverConfigManager);
				return bucket;
			}
		};
	}

	@Bean("local")
	public BucketManager localBucketManager(
			@Qualifier("localMessagePathBuilder") org.unidal.cat.message.storage.PathBuilder localMessagePathBuilder,
			@Qualifier("localMessageBucketFactory") BucketFactory localMessageBucketFactory) {
		LocalBucketManager manager = new LocalBucketManager();

		manager.setPathBuilder(localMessagePathBuilder);
		manager.setBucketFactory(localMessageBucketFactory);
		return manager;
	}

	@Bean
	public TokenMappingFactory localTokenMappingFactory(
			@Qualifier("localMessagePathBuilder") org.unidal.cat.message.storage.PathBuilder localMessagePathBuilder) {
		return (hour, ip) -> {
			LocalTokenMapping mapping = new LocalTokenMapping();

			mapping.setPathBuilder(localMessagePathBuilder);
			mapping.open(hour, ip);
			return mapping;
		};
	}

	@Bean(name = "localTokenMappingManager")
	public TokenMappingManager localTokenMappingManager(
			@Qualifier("localTokenMappingFactory") TokenMappingFactory localTokenMappingFactory) {
		LocalTokenMappingManager manager = new LocalTokenMappingManager();

		manager.setTokenMappingFactory(localTokenMappingFactory);
		return manager;
	}

	@Bean
	public IndexFactory localIndexFactory(
			@Qualifier("localMessagePathBuilder") org.unidal.cat.message.storage.PathBuilder localMessagePathBuilder,
			ByteBufCache byteBufCache,
			@Qualifier("localTokenMappingManager") TokenMappingManager localTokenMappingManager) {
		return (domain, ip, hour) -> {
			LocalIndex index = new LocalIndex();

			index.setPathBuilder(localMessagePathBuilder);
			index.setBufCache(byteBufCache);
			index.setTokenMappingManager(localTokenMappingManager);
			index.initialize(domain, ip, hour);
			return index;
		};
	}

	@Bean(name = "localIndexManager")
	public IndexManager localIndexManager(
			@Qualifier("localMessagePathBuilder") org.unidal.cat.message.storage.PathBuilder localMessagePathBuilder,
			@Qualifier("localIndexFactory") IndexFactory localIndexFactory) {
		LocalIndexManager manager = new LocalIndexManager();

		manager.setPathBuilder(localMessagePathBuilder);
		manager.setIndexFactory(localIndexFactory);
		return manager;
	}

	@Bean
	public TokenMappingFactory hdfsTokenMappingFactory(
			@Qualifier("hdfsMessagePathBuilder") org.unidal.cat.message.storage.PathBuilder hdfsMessagePathBuilder,
			HdfsSystemManager hdfsSystemManager) {
		return (hour, ip) -> {
			HdfsTokenMapping mapping = new HdfsTokenMapping();

			mapping.setPathBuilder(hdfsMessagePathBuilder);
			mapping.setFileSystemManager(hdfsSystemManager);
			mapping.open(hour, ip);
			return mapping;
		};
	}

	@Bean(name = "hdfsTokenMappingManager")
	public TokenMappingManager hdfsTokenMappingManager(
			@Qualifier("hdfsTokenMappingFactory") TokenMappingFactory hdfsTokenMappingFactory) {
		HdfsTokenMappingManager manager = new HdfsTokenMappingManager();

		manager.setTokenMappingFactory(hdfsTokenMappingFactory);
		return manager;
	}

	@Bean
	public IndexFactory hdfsIndexFactory(
			@Qualifier("hdfsMessagePathBuilder") org.unidal.cat.message.storage.PathBuilder hdfsMessagePathBuilder,
			HdfsSystemManager hdfsSystemManager, ServerConfigManager serverConfigManager,
			@Qualifier("hdfsTokenMappingManager") TokenMappingManager hdfsTokenMappingManager) {
		return (domain, ip, hour) -> {
			HdfsIndex index = new HdfsIndex();

			index.setPathBuilder(hdfsMessagePathBuilder);
			index.setFileSystemManager(hdfsSystemManager);
			index.setServerConfigManager(serverConfigManager);
			index.setTokenMappingManager(hdfsTokenMappingManager);
			index.initialize(domain, ip, hour);
			return index;
		};
	}

	@Bean
	public MessageConsumerFinder hdfsMessageConsumerFinder(HdfsSystemManager hdfsSystemManager) {
		HdfsMessageConsumerFinder finder = new HdfsMessageConsumerFinder();

		finder.setFileSystemManager(hdfsSystemManager);
		return finder;
	}

	@Bean(initMethod = "initialize")
	public HdfsIndexManager hdfsIndexManager(ServerConfigManager serverConfigManager, HdfsSystemManager hdfsSystemManager,
			@Qualifier("hdfsMessageConsumerFinder") MessageConsumerFinder hdfsMessageConsumerFinder,
			@Qualifier("hdfsIndexFactory") IndexFactory hdfsIndexFactory) {
		HdfsIndexManager manager = new HdfsIndexManager();

		manager.setConfigManager(serverConfigManager);
		manager.setFileSystemManager(hdfsSystemManager);
		manager.setConsumerFinder(hdfsMessageConsumerFinder);
		manager.setIndexFactory(hdfsIndexFactory);
		return manager;
	}

	@Bean
	public BucketFactory hdfsBucketFactory(
			@Qualifier("hdfsMessagePathBuilder") org.unidal.cat.message.storage.PathBuilder hdfsMessagePathBuilder,
			HdfsSystemManager hdfsSystemManager, ServerConfigManager serverConfigManager) {
		return (domain, ip, hour, writeMode) -> {
			HdfsBucket bucket = new HdfsBucket();

			bucket.setPathBuilder(hdfsMessagePathBuilder);
			bucket.setFileSystemManager(hdfsSystemManager);
			bucket.setServerConfigManager(serverConfigManager);
			bucket.initialize(domain, ip, hour, writeMode);
			return bucket;
		};
	}

	@Bean(initMethod = "initialize")
	public HdfsBucketManager hdfsBucketManager(ServerConfigManager serverConfigManager, HdfsSystemManager hdfsSystemManager,
			@Qualifier("hdfsMessageConsumerFinder") MessageConsumerFinder hdfsMessageConsumerFinder,
			@Qualifier("hdfsBucketFactory") BucketFactory hdfsBucketFactory) {
		HdfsBucketManager manager = new HdfsBucketManager();

		manager.setConfigManager(serverConfigManager);
		manager.setFileSystemManager(hdfsSystemManager);
		manager.setConsumerFinder(hdfsMessageConsumerFinder);
		manager.setBucketFactory(hdfsBucketFactory);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public FileSystemManager hdfsLogviewFileSystemManager(ServerConfigManager serverConfigManager) {
		FileSystemManager manager = new FileSystemManager();

		manager.setConfigManager(serverConfigManager);
		return manager;
	}

	@Bean
	public HdfsMessageBucketFactory hdfsMessageBucketFactory(FileSystemManager hdfsLogviewFileSystemManager) {
		return (type, dataFile, date) -> {
			AbstractHdfsMessageBucket bucket;

			if (HdfsMessageBucketManager.HARFS_BUCKET.equals(type)) {
				bucket = new HarfsMessageBucket();
			} else if (HdfsMessageBucketManager.HDFS_BUCKET.equals(type)) {
				bucket = new HdfsMessageBucket();
			} else {
				throw new IllegalArgumentException("Unsupported HDFS message bucket type: " + type);
			}
			bucket.setFileSystemManager(hdfsLogviewFileSystemManager);
			bucket.initialize(dataFile, date);
			return bucket;
		};
	}

	@Bean(initMethod = "initialize", name = "hdfsMessageBucketManager")
	public MessageBucketManager hdfsMessageBucketManager(FileSystemManager hdfsLogviewFileSystemManager,
			PathBuilder pathBuilder, ServerConfigManager serverConfigManager,
			HdfsMessageBucketFactory hdfsMessageBucketFactory) {
		HdfsMessageBucketManager manager = new HdfsMessageBucketManager();

		manager.setFileSystemManager(hdfsLogviewFileSystemManager);
		manager.setPathBuilder(pathBuilder);
		manager.setServerConfigManager(serverConfigManager);
		manager.setBucketFactory(hdfsMessageBucketFactory);
		return manager;
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
	public HostInfoRepository hostinfoRepository(SqlSessionTemplate sqlSessionTemplate,
	                                             TransactionTemplate transactionTemplate) {
		HostInfoRepository repository = new HostInfoRepository();

		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	@Bean
	public DailyReportRepository dailyReportRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		DailyReportRepository repository = new DailyReportRepository();

		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	@Bean
	public DailyReportContentRepository dailyReportContentRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		DailyReportContentRepository repository = new DailyReportContentRepository();

		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	@Bean
	public HourlyReportRepository hourlyReportRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		HourlyReportRepository repository = new HourlyReportRepository();

		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	@Bean
	public HourlyReportContentRepository hourlyReportContentRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		HourlyReportContentRepository repository = new HourlyReportContentRepository();

		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	@Bean
	public WeeklyReportRepository weeklyReportRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		WeeklyReportRepository repository = new WeeklyReportRepository();

		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	@Bean
	public WeeklyReportContentRepository weeklyReportContentRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		WeeklyReportContentRepository repository = new WeeklyReportContentRepository();

		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	@Bean
	public MonthlyReportRepository monthlyReportRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		MonthlyReportRepository repository = new MonthlyReportRepository();

		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	@Bean
	public MonthlyReportContentRepository monthlyReportContentRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		MonthlyReportContentRepository repository = new MonthlyReportContentRepository();

		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	@Bean
	public OverloadRepository overloadRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		return configureSpringBackedRepository(new OverloadRepository(), sqlSessionTemplate, transactionTemplate);
	}

	@Bean
	public AlertRepository alertRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		return configureSpringBackedRepository(new AlertRepository(), sqlSessionTemplate, transactionTemplate);
	}

	@Bean
	public AlterationRepository alterationRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		return configureSpringBackedRepository(new AlterationRepository(), sqlSessionTemplate, transactionTemplate);
	}

	@Bean
	public BaselineRepository baselineRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		return configureSpringBackedRepository(new BaselineRepository(), sqlSessionTemplate, transactionTemplate);
	}

	@Bean
	public TopologyGraphRepository topologyGraphRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		return configureSpringBackedRepository(new TopologyGraphRepository(), sqlSessionTemplate, transactionTemplate);
	}

	@Bean
	public TaskRepository taskRepository(SqlSessionTemplate sqlSessionTemplate, TransactionTemplate transactionTemplate) {
		TaskRepository repository = new TaskRepository();

		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	@Bean
	public AlertSummaryRepository alertSummaryRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		return configureSpringBackedRepository(new AlertSummaryRepository(), sqlSessionTemplate, transactionTemplate);
	}

	@Bean
	public ConfigModificationRepository configModificationRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		return configureSpringBackedRepository(new ConfigModificationRepository(), sqlSessionTemplate, transactionTemplate);
	}

	@Bean
	public MetricGraphRepository metricGraphRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		return configureSpringBackedRepository(new MetricGraphRepository(), sqlSessionTemplate, transactionTemplate);
	}

	@Bean
	public MetricScreenRepository metricScreenRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		return configureSpringBackedRepository(new MetricScreenRepository(), sqlSessionTemplate, transactionTemplate);
	}

	@Bean
	public ServerAlarmRuleRepository serverAlarmRuleRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		return configureSpringBackedRepository(new ServerAlarmRuleRepository(), sqlSessionTemplate, transactionTemplate);
	}

	@Bean
	public UserDefineRuleRepository userDefineRuleRepository(SqlSessionTemplate sqlSessionTemplate,
			TransactionTemplate transactionTemplate) {
		return configureSpringBackedRepository(new UserDefineRuleRepository(), sqlSessionTemplate, transactionTemplate);
	}

	@Bean
	public DataSource catDataSource() {
		return CatHomeSpringDataSourceFactory.createCatDataSource();
	}

	private <T extends SpringBackedRepositorySupport<?>> T configureSpringBackedRepository(T repository,
			SqlSessionTemplate sqlSessionTemplate, TransactionTemplate transactionTemplate) {
		repository.setSqlSessionTemplate(sqlSessionTemplate);
		repository.setTransactionTemplate(transactionTemplate);
		return repository;
	}

	private void configureReportService(AbstractReportService<?> service, HourlyReportRepository hourlyReportRepository,
			HourlyReportContentRepository hourlyReportContentRepository, DailyReportRepository dailyReportRepository,
			DailyReportContentRepository dailyReportContentRepository, WeeklyReportRepository weeklyReportRepository,
			WeeklyReportContentRepository weeklyReportContentRepository, MonthlyReportRepository monthlyReportRepository,
			MonthlyReportContentRepository monthlyReportContentRepository) {
		service.setHourlyReportDao(hourlyReportRepository);
		service.setHourlyReportContentDao(hourlyReportContentRepository);
		service.setDailyReportDao(dailyReportRepository);
		service.setDailyReportContentDao(dailyReportContentRepository);
		service.setWeeklyReportDao(weeklyReportRepository);
		service.setWeeklyReportContentDao(weeklyReportContentRepository);
		service.setMonthlyReportDao(monthlyReportRepository);
		service.setMonthlyReportContentDao(monthlyReportContentRepository);
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
