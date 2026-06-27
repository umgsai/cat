package com.dianping.cat.home.spring;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
import com.dianping.cat.consumer.problem.ProblemHandler;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.StateDelegate;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.StorageDelegate;
import com.dianping.cat.consumer.storage.StorageReportUpdater;
import com.dianping.cat.consumer.storage.builder.StorageBuilder;
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
import com.dianping.cat.alarm.spi.decorator.Decorator;
import com.dianping.cat.alarm.spi.decorator.DecoratorManager;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.alarm.spi.receiver.ContactorManager;
import com.dianping.cat.alarm.spi.rule.DataChecker;
import com.dianping.cat.alarm.spi.rule.DefaultDataChecker;
import com.dianping.cat.alarm.service.AlertService;
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
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelService;
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
import com.dianping.cat.system.page.login.service.CatPropertyProvider;
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
@ComponentScan(basePackageClasses = {BusinessAnalyzer.class, TransactionAnalyzer.class, CrossAnalyzer.class,
		DumpAnalyzer.class, DependencyAnalyzer.class, EventAnalyzer.class, HeartbeatAnalyzer.class,
		MatrixAnalyzer.class, ProblemAnalyzer.class, StorageAnalyzer.class, TopAnalyzer.class, StateAnalyzer.class,
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
		AlertSummaryService.class, AlertService.class, DefaultDataChecker.class, BaseRuleHelper.class,
		AlertInfoBuilder.class, UserDefinedRuleManager.class, DefaultBaselineService.class,
		DataExtractorImpl.class, EventMergeHelper.class, TransactionMergeHelper.class,
		LocalResourceContentFetcher.class, DefaultPathBuilder.class, ServerStatisticManager.class,
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
				classes = {BusinessAnalyzer.class, TransactionAnalyzer.class, CrossAnalyzer.class, DumpAnalyzer.class,
					DependencyAnalyzer.class, EventAnalyzer.class, HeartbeatAnalyzer.class, MatrixAnalyzer.class,
					ProblemAnalyzer.class, StorageAnalyzer.class, TopAnalyzer.class, StateAnalyzer.class,
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
					AlertSummaryService.class, AlertService.class, DefaultDataChecker.class, BaseRuleHelper.class,
					AlertInfoBuilder.class, UserDefinedRuleManager.class, DefaultBaselineService.class,
					DataExtractorImpl.class, EventMergeHelper.class, TransactionMergeHelper.class,
					LocalResourceContentFetcher.class, DefaultPathBuilder.class, ServerStatisticManager.class,
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

	@Bean
	public TaskManager taskManager(TaskRepository taskRepository) {
		TaskManager manager = new TaskManager();

		manager.setTaskDao(taskRepository);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public AllReportConfigManager allReportConfigManager(ConfigRepository configRepository,
			ContentFetcher contentFetcher) {
		AllReportConfigManager manager = new AllReportConfigManager();

		manager.setConfigDao(configRepository);
		manager.setFetcher(contentFetcher);
		return manager;
	}

	@Bean
	public ReportDelegate<BusinessReport> businessDelegate(TaskManager taskManager) {
		BusinessDelegate delegate = new BusinessDelegate();

		delegate.setTaskManager(taskManager);
		return delegate;
	}

	@Bean
	public ReportDelegate<TransactionReport> transactionDelegate(TaskManager taskManager,
			ServerFilterConfigManager serverFilterConfigManager, AllReportConfigManager allReportConfigManager,
			ServerConfigManager serverConfigManager, AtomicMessageConfigManager atomicMessageConfigManager) {
		TransactionDelegate delegate = new TransactionDelegate();

		delegate.setTaskManager(taskManager);
		delegate.setConfigManager(serverFilterConfigManager);
		delegate.setTransactionManager(allReportConfigManager);
		delegate.setServerConfigManager(serverConfigManager);
		delegate.setAtomicMessageConfigManager(atomicMessageConfigManager);
		return delegate;
	}

	@Bean
	public ReportDelegate<CrossReport> crossDelegate(TaskManager taskManager,
			ServerFilterConfigManager serverFilterConfigManager) {
		CrossDelegate delegate = new CrossDelegate();

		delegate.setTaskManager(taskManager);
		delegate.setServerFilterConfigManager(serverFilterConfigManager);
		return delegate;
	}

	@Bean
	public ReportDelegate<DependencyReport> dependencyDelegate(TaskManager taskManager) {
		DependencyDelegate delegate = new DependencyDelegate();

		delegate.setTaskManager(taskManager);
		return delegate;
	}

	@Bean
	public ReportDelegate<EventReport> eventDelegate(TaskManager taskManager,
			ServerFilterConfigManager serverFilterConfigManager, AllReportConfigManager allReportConfigManager,
			ServerConfigManager serverConfigManager, AtomicMessageConfigManager atomicMessageConfigManager) {
		EventDelegate delegate = new EventDelegate();

		delegate.setTaskManager(taskManager);
		delegate.setConfigManager(serverFilterConfigManager);
		delegate.setAllManager(allReportConfigManager);
		delegate.setServerConfigManager(serverConfigManager);
		delegate.setAtomicMessageConfigManager(atomicMessageConfigManager);
		return delegate;
	}

	@Bean
	public ReportDelegate<HeartbeatReport> heartbeatDelegate(TaskManager taskManager,
			ServerFilterConfigManager serverFilterConfigManager) {
		HeartbeatDelegate delegate = new HeartbeatDelegate();

		delegate.setTaskManager(taskManager);
		delegate.setConfigManager(serverFilterConfigManager);
		return delegate;
	}

	@Bean
	public ReportDelegate<MatrixReport> matrixDelegate(TaskManager taskManager,
			ServerFilterConfigManager serverFilterConfigManager) {
		MatrixDelegate delegate = new MatrixDelegate();

		delegate.setTaskManager(taskManager);
		delegate.setConfigManager(serverFilterConfigManager);
		return delegate;
	}

	@Bean
	public ReportDelegate<ProblemReport> problemDelegate(TaskManager taskManager,
			ServerFilterConfigManager serverFilterConfigManager) {
		ProblemDelegate delegate = new ProblemDelegate();

		delegate.setTaskManager(taskManager);
		delegate.setConfigManager(serverFilterConfigManager);
		return delegate;
	}

	@Bean
	public StorageReportUpdater storageReportUpdater() {
		return new StorageReportUpdater();
	}

	@Bean
	public ReportDelegate<StorageReport> storageDelegate(TaskManager taskManager,
			ServerFilterConfigManager serverFilterConfigManager, StorageReportUpdater storageReportUpdater) {
		StorageDelegate delegate = new StorageDelegate();

		delegate.setTaskManager(taskManager);
		delegate.setConfigManager(serverFilterConfigManager);
		delegate.setReportUpdater(storageReportUpdater);
		return delegate;
	}

	@Bean
	public ReportDelegate<TopReport> topDelegate() {
		return new TopDelegate();
	}

	@Bean
	public ReportDelegate<StateReport> stateDelegate(TaskManager taskManager, ReportBucketManager reportBucketManager) {
		StateDelegate delegate = new StateDelegate();

		delegate.setTaskManager(taskManager);
		delegate.setBucketManager(reportBucketManager);
		return delegate;
	}

	@Bean(name = BusinessAnalyzer.ID + "ReportManager", initMethod = "initialize")
	@Scope("prototype")
	public ReportManager<BusinessReport> businessReportManager(ReportDelegate<BusinessReport> businessDelegate,
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
			ReportDelegate<TransactionReport> transactionDelegate, ReportBucketManager reportBucketManager,
			HourlyReportRepository hourlyReportRepository, HourlyReportContentRepository hourlyReportContentRepository,
			DomainValidator domainValidator) {
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
	public ReportManager<CrossReport> crossReportManager(ReportDelegate<CrossReport> crossDelegate,
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
			ReportDelegate<DependencyReport> dependencyDelegate, ReportBucketManager reportBucketManager,
			HourlyReportRepository hourlyReportRepository, HourlyReportContentRepository hourlyReportContentRepository,
			DomainValidator domainValidator) {
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
	public ReportManager<EventReport> eventReportManager(ReportDelegate<EventReport> eventDelegate,
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
	public ReportManager<HeartbeatReport> heartbeatReportManager(ReportDelegate<HeartbeatReport> heartbeatDelegate,
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
	public ReportManager<MatrixReport> matrixReportManager(ReportDelegate<MatrixReport> matrixDelegate,
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
	public ReportManager<ProblemReport> problemReportManager(ReportDelegate<ProblemReport> problemDelegate,
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
	public ReportManager<StorageReport> storageReportManager(ReportDelegate<StorageReport> storageDelegate,
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
	public ReportManager<TopReport> topReportManager(ReportDelegate<TopReport> topDelegate,
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
	public ReportManager<StateReport> stateReportManager(ReportDelegate<StateReport> stateDelegate,
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
	public ReportReloader businessReportReloader(
			@Qualifier(BusinessAnalyzer.ID + "ReportManager") ReportManager<BusinessReport> businessReportManager,
			HourlyReportRepository hourlyReportRepository, HourlyReportContentRepository hourlyReportContentRepository,
			ServerConfigManager serverConfigManager) {
		BusinessReportReloader reloader = new BusinessReportReloader();

		reloader.setReportManager(businessReportManager);
		configureReportReloader(reloader, hourlyReportRepository, hourlyReportContentRepository, serverConfigManager);
		return reloader;
	}

	@Bean
	public ReportReloader transactionReportReloader(
			@Qualifier(TransactionAnalyzer.ID + "ReportManager") ReportManager<TransactionReport> transactionReportManager,
			HourlyReportRepository hourlyReportRepository, HourlyReportContentRepository hourlyReportContentRepository,
			ServerConfigManager serverConfigManager) {
		TransactionReportReloader reloader = new TransactionReportReloader();

		reloader.setReportManager(transactionReportManager);
		configureReportReloader(reloader, hourlyReportRepository, hourlyReportContentRepository, serverConfigManager);
		return reloader;
	}

	@Bean
	public ReportReloader crossReportReloader(
			@Qualifier(CrossAnalyzer.ID + "ReportManager") ReportManager<CrossReport> crossReportManager,
			HourlyReportRepository hourlyReportRepository, HourlyReportContentRepository hourlyReportContentRepository,
			ServerConfigManager serverConfigManager) {
		CrossReportReloader reloader = new CrossReportReloader();

		reloader.setReportManager(crossReportManager);
		configureReportReloader(reloader, hourlyReportRepository, hourlyReportContentRepository, serverConfigManager);
		return reloader;
	}

	@Bean
	public ReportReloader dependencyReportReloader(
			@Qualifier(DependencyAnalyzer.ID + "ReportManager") ReportManager<DependencyReport> dependencyReportManager,
			HourlyReportRepository hourlyReportRepository, HourlyReportContentRepository hourlyReportContentRepository,
			ServerConfigManager serverConfigManager) {
		DependencyReportReloader reloader = new DependencyReportReloader();

		reloader.setReportManager(dependencyReportManager);
		configureReportReloader(reloader, hourlyReportRepository, hourlyReportContentRepository, serverConfigManager);
		return reloader;
	}

	@Bean
	public ReportReloader eventReportReloader(
			@Qualifier(EventAnalyzer.ID + "ReportManager") ReportManager<EventReport> eventReportManager,
			HourlyReportRepository hourlyReportRepository, HourlyReportContentRepository hourlyReportContentRepository,
			ServerConfigManager serverConfigManager) {
		EventReportReloader reloader = new EventReportReloader();

		reloader.setReportManager(eventReportManager);
		configureReportReloader(reloader, hourlyReportRepository, hourlyReportContentRepository, serverConfigManager);
		return reloader;
	}

	@Bean
	public ReportReloader heartbeatReportReloader(
			@Qualifier(HeartbeatAnalyzer.ID + "ReportManager") ReportManager<HeartbeatReport> heartbeatReportManager,
			HourlyReportRepository hourlyReportRepository, HourlyReportContentRepository hourlyReportContentRepository,
			ServerConfigManager serverConfigManager) {
		HeartbeatReportReloader reloader = new HeartbeatReportReloader();

		reloader.setReportManager(heartbeatReportManager);
		configureReportReloader(reloader, hourlyReportRepository, hourlyReportContentRepository, serverConfigManager);
		return reloader;
	}

	@Bean
	public ReportReloader matrixReportReloader(
			@Qualifier(MatrixAnalyzer.ID + "ReportManager") ReportManager<MatrixReport> matrixReportManager,
			HourlyReportRepository hourlyReportRepository, HourlyReportContentRepository hourlyReportContentRepository,
			ServerConfigManager serverConfigManager) {
		MatrixReportReloader reloader = new MatrixReportReloader();

		reloader.setReportManager(matrixReportManager);
		configureReportReloader(reloader, hourlyReportRepository, hourlyReportContentRepository, serverConfigManager);
		return reloader;
	}

	@Bean
	public ReportReloader problemReportReloader(
			@Qualifier(ProblemAnalyzer.ID + "ReportManager") ReportManager<ProblemReport> problemReportManager,
			HourlyReportRepository hourlyReportRepository, HourlyReportContentRepository hourlyReportContentRepository,
			ServerConfigManager serverConfigManager) {
		ProblemReportReloader reloader = new ProblemReportReloader();

		reloader.setReportManager(problemReportManager);
		configureReportReloader(reloader, hourlyReportRepository, hourlyReportContentRepository, serverConfigManager);
		return reloader;
	}

	@Bean
	public ReportReloader storageReportReloader(
			@Qualifier(StorageAnalyzer.ID + "ReportManager") ReportManager<StorageReport> storageReportManager,
			HourlyReportRepository hourlyReportRepository, HourlyReportContentRepository hourlyReportContentRepository,
			ServerConfigManager serverConfigManager) {
		StorageReportReloader reloader = new StorageReportReloader();

		reloader.setReportManager(storageReportManager);
		configureReportReloader(reloader, hourlyReportRepository, hourlyReportContentRepository, serverConfigManager);
		return reloader;
	}

	@Bean
	public ReportReloader topReportReloader(
			@Qualifier(TopAnalyzer.ID + "ReportManager") ReportManager<TopReport> topReportManager,
			HourlyReportRepository hourlyReportRepository, HourlyReportContentRepository hourlyReportContentRepository,
			ServerConfigManager serverConfigManager) {
		TopReportReloader reloader = new TopReportReloader();

		reloader.setReportManager(topReportManager);
		configureReportReloader(reloader, hourlyReportRepository, hourlyReportContentRepository, serverConfigManager);
		return reloader;
	}

	@Bean
	public ReportReloader stateReportReloader(
			@Qualifier(StateAnalyzer.ID + "ReportManager") ReportManager<StateReport> stateReportManager,
			HourlyReportRepository hourlyReportRepository, HourlyReportContentRepository hourlyReportContentRepository,
			ServerConfigManager serverConfigManager) {
		StateReportReloader reloader = new StateReportReloader();

		reloader.setReportManager(stateReportManager);
		configureReportReloader(reloader, hourlyReportRepository, hourlyReportContentRepository, serverConfigManager);
		return reloader;
	}

	private void configureReportReloader(AbstractReportReloader reloader, HourlyReportRepository hourlyReportRepository,
			HourlyReportContentRepository hourlyReportContentRepository, ServerConfigManager serverConfigManager) {
		reloader.setHourlyReportDao(hourlyReportRepository);
		reloader.setHourlyReportContentDao(hourlyReportContentRepository);
		reloader.setServerConfigManager(serverConfigManager);
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

	@Bean(initMethod = "initialize")
	public ReportReloadTask reportReloadTask(ReportReloadConfigManager reportReloadConfigManager,
			@Qualifier("reportReloaders") Map<String, ReportReloader> reportReloaders) {
		ReportReloadTask task = new ReportReloadTask();

		task.setConfigManager(reportReloadConfigManager);
		task.setReloaders(reportReloaders);
		return task;
	}

	@Bean(initMethod = "initialize")
	public ReportFacade reportFacade(Map<String, TaskBuilder> taskBuilders) {
		ReportFacade facade = new ReportFacade();

		facade.setReportBuilders(taskBuilders);
		return facade;
	}

	@Bean
	public DefaultTaskConsumer defaultTaskConsumer(ReportFacade reportFacade, TaskRepository taskRepository) {
		DefaultTaskConsumer consumer = new DefaultTaskConsumer();

		consumer.setReportFacade(reportFacade);
		consumer.setTaskDao(taskRepository);
		return consumer;
	}

	@Bean(name = CurrentReportBuilder.ID)
	public TaskBuilder currentReportBuilder(ProjectService projectService,
			ServerFilterConfigManager serverFilterConfigManager) {
		CurrentReportBuilder builder = new CurrentReportBuilder();

		builder.setProjectService(projectService);
		builder.setServerFilterConfigManager(serverFilterConfigManager);
		return builder;
	}

	@Bean
	public ProjectUpdateTask projectUpdateTask(HostinfoService hostinfoService, ProjectService projectService,
			TransactionReportService transactionReportService) {
		ProjectUpdateTask task = new ProjectUpdateTask();

		task.setHostInfoService(hostinfoService);
		task.setProjectService(projectService);
		task.setReportService(transactionReportService);
		return task;
	}

	@Bean(name = CmdbInfoReloadBuilder.ID)
	public TaskBuilder cmdbInfoReloadBuilder(ProjectUpdateTask projectUpdateTask) {
		CmdbInfoReloadBuilder builder = new CmdbInfoReloadBuilder();

		builder.setProjectUpdateTask(projectUpdateTask);
		return builder;
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
	public List<ProblemHandler> problemHandlers(@Qualifier(DefaultProblemHandler.ID) ProblemHandler defaultProblemHandler,
			@Qualifier(LongExecutionProblemHandler.ID) ProblemHandler longExecutionProblemHandler) {
		return Arrays.asList(defaultProblemHandler, longExecutionProblemHandler);
	}

	@Bean
	public Map<String, StorageBuilder> storageBuilders(@Qualifier("storageSQLBuilder") StorageBuilder storageSQLBuilder,
			@Qualifier("storageCacheBuilder") StorageBuilder storageCacheBuilder,
			@Qualifier("storageRPCBuilder") StorageBuilder storageRPCBuilder) {
		return buildStorageBuilders(storageSQLBuilder, storageCacheBuilder, storageRPCBuilder);
	}

	@Bean(initMethod = "initialize")
	public StorageBuilderManager storageBuilderManager(@Qualifier("storageSQLBuilder") StorageBuilder storageSQLBuilder,
			@Qualifier("storageCacheBuilder") StorageBuilder storageCacheBuilder,
			@Qualifier("storageRPCBuilder") StorageBuilder storageRPCBuilder) {
		StorageBuilderManager manager = new StorageBuilderManager();

		manager.setStorageBuilders(buildStorageBuilders(storageSQLBuilder, storageCacheBuilder, storageRPCBuilder));
		return manager;
	}

	@Bean(initMethod = "initialize", name = "problem-historical")
	public ModelService<ProblemReport> historicalProblemService(ProblemReportService problemReportService,
			ServerConfigManager serverConfigManager) {
		HistoricalProblemService service = new HistoricalProblemService();

		service.setReportService(problemReportService);
		service.setConfigManager(serverConfigManager);
		return service;
	}

	@Bean(initMethod = "initialize", name = "business-historical")
	public ModelService<BusinessReport> historicalBusinessService(BusinessReportService businessReportService,
			ServerConfigManager serverConfigManager) {
		HistoricalBusinessService service = new HistoricalBusinessService();

		service.setReportService(businessReportService);
		service.setConfigManager(serverConfigManager);
		return service;
	}

	@Bean(initMethod = "initialize", name = "event-historical")
	public ModelService<EventReport> historicalEventService(EventReportService eventReportService,
			ServerConfigManager serverConfigManager) {
		HistoricalEventService service = new HistoricalEventService();

		service.setReportService(eventReportService);
		service.setConfigManager(serverConfigManager);
		return service;
	}

	@Bean(initMethod = "initialize", name = "transaction-historical")
	public ModelService<TransactionReport> historicalTransactionService(
			TransactionReportService transactionReportService, ServerConfigManager serverConfigManager) {
		HistoricalTransactionService service = new HistoricalTransactionService();

		service.setReportService(transactionReportService);
		service.setConfigManager(serverConfigManager);
		return service;
	}

	@Bean(initMethod = "initialize", name = "heartbeat-historical")
	public ModelService<HeartbeatReport> historicalHeartbeatService(HeartbeatReportService heartbeatReportService,
			ServerConfigManager serverConfigManager) {
		HistoricalHeartbeatService service = new HistoricalHeartbeatService();

		service.setReportService(heartbeatReportService);
		service.setConfigManager(serverConfigManager);
		return service;
	}

	@Bean(initMethod = "initialize", name = "top-historical")
	public ModelService<TopReport> historicalTopService(TopReportService topReportService,
			ServerConfigManager serverConfigManager) {
		HistoricalTopService service = new HistoricalTopService();

		service.setReportService(topReportService);
		service.setConfigManager(serverConfigManager);
		return service;
	}

	@Bean(initMethod = "initialize", name = "state-historical")
	public ModelService<StateReport> historicalStateService(StateReportService stateReportService,
			ServerConfigManager serverConfigManager) {
		HistoricalStateService service = new HistoricalStateService();

		service.setReportService(stateReportService);
		service.setConfigManager(serverConfigManager);
		return service;
	}

	@Bean(initMethod = "initialize", name = "storage-historical")
	public ModelService<StorageReport> historicalStorageService(StorageReportService storageReportService,
			ServerConfigManager serverConfigManager) {
		HistoricalStorageService service = new HistoricalStorageService();

		service.setReportService(storageReportService);
		service.setConfigManager(serverConfigManager);
		return service;
	}

	@Bean(initMethod = "initialize", name = "cross-historical")
	public ModelService<CrossReport> historicalCrossService(CrossReportService crossReportService,
			ServerConfigManager serverConfigManager) {
		HistoricalCrossService service = new HistoricalCrossService();

		service.setReportService(crossReportService);
		service.setConfigManager(serverConfigManager);
		return service;
	}

	@Bean(initMethod = "initialize", name = "matrix-historical")
	public ModelService<MatrixReport> historicalMatrixService(MatrixReportService matrixReportService,
			ServerConfigManager serverConfigManager) {
		HistoricalMatrixService service = new HistoricalMatrixService();

		service.setReportService(matrixReportService);
		service.setConfigManager(serverConfigManager);
		return service;
	}

	@Bean(initMethod = "initialize", name = "dependency-historical")
	public ModelService<DependencyReport> historicalDependencyService(DependencyReportService dependencyReportService,
			ServerConfigManager serverConfigManager) {
		HistoricalDependencyService service = new HistoricalDependencyService();

		service.setReportService(dependencyReportService);
		service.setConfigManager(serverConfigManager);
		return service;
	}

	@Bean(initMethod = "initialize")
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

	@Bean(initMethod = "initialize", name = "businessModelService")
	public ModelService<BusinessReport> businessModelService(
			@Qualifier("business-historical") ModelService<BusinessReport> historicalBusinessService,
			ServerConfigManager serverConfigManager, RemoteServersManager remoteServersManager) {
		CompositeBusinessService service = new CompositeBusinessService();
		List<ModelService<BusinessReport>> services = Collections.singletonList(historicalBusinessService);

		service.setServices(services);
		service.setConfigManager(serverConfigManager);
		service.setServerManager(remoteServersManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public ModelService<EventReport> eventModelService(
			@Qualifier("event-historical") ModelService<EventReport> historicalEventService,
			ServerConfigManager serverConfigManager, RemoteServersManager remoteServersManager) {
		CompositeEventService service = new CompositeEventService();
		List<ModelService<EventReport>> services = Collections.singletonList(historicalEventService);

		service.setServices(services);
		service.setConfigManager(serverConfigManager);
		service.setServerManager(remoteServersManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public ModelService<TransactionReport> transactionModelService(
			@Qualifier("transaction-historical") ModelService<TransactionReport> historicalTransactionService,
			ServerConfigManager serverConfigManager, RemoteServersManager remoteServersManager) {
		CompositeTransactionService service = new CompositeTransactionService();
		List<ModelService<TransactionReport>> services = Collections.singletonList(historicalTransactionService);

		service.setServices(services);
		service.setConfigManager(serverConfigManager);
		service.setServerManager(remoteServersManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public ModelService<HeartbeatReport> heartbeatModelService(
			@Qualifier("heartbeat-historical") ModelService<HeartbeatReport> historicalHeartbeatService,
			ServerConfigManager serverConfigManager, RemoteServersManager remoteServersManager) {
		CompositeHeartbeatService service = new CompositeHeartbeatService();
		List<ModelService<HeartbeatReport>> services = Collections.singletonList(historicalHeartbeatService);

		service.setServices(services);
		service.setConfigManager(serverConfigManager);
		service.setServerManager(remoteServersManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public ModelService<TopReport> topModelService(
			@Qualifier("top-historical") ModelService<TopReport> historicalTopService,
			ServerConfigManager serverConfigManager, RemoteServersManager remoteServersManager) {
		CompositeTopService service = new CompositeTopService();
		List<ModelService<TopReport>> services = Collections.singletonList(historicalTopService);

		service.setServices(services);
		service.setConfigManager(serverConfigManager);
		service.setServerManager(remoteServersManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public ModelService<StateReport> stateModelService(
			@Qualifier("state-historical") ModelService<StateReport> historicalStateService,
			ServerConfigManager serverConfigManager, RemoteServersManager remoteServersManager) {
		CompositeStateService service = new CompositeStateService();
		List<ModelService<StateReport>> services = Collections.singletonList(historicalStateService);

		service.setServices(services);
		service.setConfigManager(serverConfigManager);
		service.setServerManager(remoteServersManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public ModelService<StorageReport> storageModelService(
			@Qualifier("storage-historical") ModelService<StorageReport> historicalStorageService,
			ServerConfigManager serverConfigManager, RemoteServersManager remoteServersManager) {
		CompositeStorageService service = new CompositeStorageService();
		List<ModelService<StorageReport>> services = Collections.singletonList(historicalStorageService);

		service.setServices(services);
		service.setConfigManager(serverConfigManager);
		service.setServerManager(remoteServersManager);
		return service;
	}

	@Bean(initMethod = "initialize", name = "crossModelService")
	public ModelService<CrossReport> crossModelService(
			@Qualifier("cross-historical") ModelService<CrossReport> historicalCrossService,
			ServerConfigManager serverConfigManager, RemoteServersManager remoteServersManager) {
		CompositeCrossService service = new CompositeCrossService();
		List<ModelService<CrossReport>> services = Collections.singletonList(historicalCrossService);

		service.setServices(services);
		service.setConfigManager(serverConfigManager);
		service.setServerManager(remoteServersManager);
		return service;
	}

	@Bean(initMethod = "initialize", name = "matrixModelService")
	public ModelService<MatrixReport> matrixModelService(
			@Qualifier("matrix-historical") ModelService<MatrixReport> historicalMatrixService,
			ServerConfigManager serverConfigManager, RemoteServersManager remoteServersManager) {
		CompositeMatrixService service = new CompositeMatrixService();
		List<ModelService<MatrixReport>> services = Collections.singletonList(historicalMatrixService);

		service.setServices(services);
		service.setConfigManager(serverConfigManager);
		service.setServerManager(remoteServersManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public ModelService<DependencyReport> dependencyModelService(
			@Qualifier("dependency-historical") ModelService<DependencyReport> historicalDependencyService,
			ServerConfigManager serverConfigManager, RemoteServersManager remoteServersManager) {
		CompositeDependencyService service = new CompositeDependencyService();
		List<ModelService<DependencyReport>> services = Collections.singletonList(historicalDependencyService);

		service.setServices(services);
		service.setConfigManager(serverConfigManager);
		service.setServerManager(remoteServersManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<ProblemReport> localProblemService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager, MessageConsumer messageConsumer) {
		LocalProblemService service = new LocalProblemService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		service.setConsumer(messageConsumer);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<EventReport> localEventService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager, MessageConsumer messageConsumer) {
		LocalEventService service = new LocalEventService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		service.setConsumer(messageConsumer);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<TransactionReport> localTransactionService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager, MessageConsumer messageConsumer) {
		LocalTransactionService service = new LocalTransactionService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		service.setConsumer(messageConsumer);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<HeartbeatReport> localHeartbeatService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager, MessageConsumer messageConsumer) {
		LocalHeartbeatService service = new LocalHeartbeatService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		service.setConsumer(messageConsumer);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<CrossReport> localCrossService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager, MessageConsumer messageConsumer) {
		LocalCrossService service = new LocalCrossService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		service.setConsumer(messageConsumer);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<MatrixReport> localMatrixService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager, MessageConsumer messageConsumer) {
		LocalMatrixService service = new LocalMatrixService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		service.setConsumer(messageConsumer);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<DependencyReport> localDependencyService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager, MessageConsumer messageConsumer) {
		LocalDependencyService service = new LocalDependencyService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		service.setConsumer(messageConsumer);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<TopReport> localTopService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager, MessageConsumer messageConsumer) {
		LocalTopService service = new LocalTopService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		service.setConsumer(messageConsumer);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<StateReport> localStateService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager, MessageConsumer messageConsumer) {
		LocalStateService service = new LocalStateService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		service.setConsumer(messageConsumer);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<StorageReport> localStorageService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager, MessageConsumer messageConsumer) {
		LocalStorageService service = new LocalStorageService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		service.setConsumer(messageConsumer);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalModelService<BusinessReport> localBusinessService(ServerConfigManager serverConfigManager,
			ReportBucketManager reportBucketManager, MessageConsumer messageConsumer) {
		LocalBusinessService service = new LocalBusinessService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(reportBucketManager);
		service.setConsumer(messageConsumer);
		return service;
	}

	@Bean(initMethod = "initialize")
	public LocalMessageService localMessageService(ServerConfigManager serverConfigManager,
			MessageFinderManager messageFinderManager, @Qualifier("local") BucketManager localBucketManager,
			@Qualifier("legacyLocalMessageBucketManager") MessageBucketManager localMessageBucketManager,
			MessageConsumer messageConsumer) {
		LocalMessageService service = new LocalMessageService();

		service.setConfigManager(serverConfigManager);
		service.setFinderManager(messageFinderManager);
		service.setBucketManager(localBucketManager);
		service.setMessageBucketManager(localMessageBucketManager);
		service.setConsumer(messageConsumer);
		return service;
	}

	@Bean(initMethod = "initialize", name = "historicalMessageService")
	public ModelService<String> historicalMessageService(ServerConfigManager serverConfigManager,
			HdfsBucketManager hdfsBucketManager,
			@Qualifier("hdfsMessageBucketManager") MessageBucketManager hdfsMessageBucketManager) {
		HistoricalMessageService service = new HistoricalMessageService();

		service.setConfigManager(serverConfigManager);
		service.setBucketManager(hdfsBucketManager);
		service.setHdfsBucketManager(hdfsMessageBucketManager);
		return service;
	}

	@Bean(initMethod = "initialize", name = "logviewModelService")
	public ModelService<String> logviewModelService(ServerConfigManager serverConfigManager,
			RemoteServersManager remoteServersManager,
			@Qualifier("localMessageService") LocalMessageService localMessageService,
			@Qualifier("historicalMessageService") ModelService<String> historicalMessageService) {
		CompositeLogViewService service = new CompositeLogViewService();
		List<ModelService<String>> services = Arrays.asList(localMessageService, historicalMessageService);

		service.setServices(services);
		service.setConfigManager(serverConfigManager);
		service.setServerManager(remoteServersManager);
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

	@Bean(initMethod = "initialize", name = RelatedSummaryBuilder.ID)
	public SummaryBuilder relatedSummaryBuilder(AlertInfoBuilder alertInfoBuilder,
			AlertSummaryService alertSummaryService) {
		RelatedSummaryBuilder builder = new RelatedSummaryBuilder();

		builder.setAlertSummaryManager(alertInfoBuilder);
		builder.setAlertSummaryService(alertSummaryService);
		return builder;
	}

	@Bean(initMethod = "initialize", name = FailureSummaryBuilder.ID)
	public SummaryBuilder failureSummaryBuilder(@Qualifier("problemModelService") ModelService<ProblemReport> problemModelService) {
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
	public RemoteServersManager remoteServersManager() {
		return new RemoteServersManager();
	}

	@Bean
	public ServersUpdater remoteServersUpdater(
			@Qualifier("localStateService") LocalModelService<StateReport> stateModelService) {
		DefaultRemoteServersUpdater updater = new DefaultRemoteServersUpdater();

		updater.setLocalService(stateModelService);
		return updater;
	}

	@Bean(initMethod = "initialize")
	public ServersUpdaterManager serversUpdaterManager(ServersUpdater remoteServersUpdater,
			RemoteServersManager remoteServersManager) {
		ServersUpdaterManager manager = new ServersUpdaterManager();

		manager.setRemoteServerUpdater(remoteServersUpdater);
		manager.setRemoteServersManager(remoteServersManager);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public com.dianping.cat.alarm.spi.decorator.RuleFTLDecorator ruleFTLDecorator() {
		return new com.dianping.cat.alarm.spi.decorator.RuleFTLDecorator();
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
	public CatPropertyProvider catPropertyProvider() {
		return new DefaultCatPropertyProvider();
	}

	@Bean
	public TokenManager tokenManager(CookieManager cookieManager, TokenBuilder tokenBuilder) {
		TokenManager manager = new TokenManager();

		manager.setCookieManager(cookieManager);
		manager.setTokenBuilder(tokenBuilder);
		return manager;
	}

	@Bean(initMethod = "initialize")
	public SessionManager sessionManager(CatPropertyProvider catPropertyProvider) {
		SessionManager manager = new SessionManager();

		manager.setProvider(catPropertyProvider);
		return manager;
	}

	@Bean
	public SigninService signinService(SessionManager sessionManager, TokenManager tokenManager) {
		SigninService service = new SigninService();

		service.setSessionManager(sessionManager);
		service.setTokenManager(tokenManager);
		return service;
	}

	@Bean(initMethod = "initialize")
	public ProjectService projectService(ProjectRepository projectRepository, ServerConfigManager serverConfigManager) {
		ProjectService service = new ProjectService();

		service.setProjectDao(projectRepository);
		service.setServerConfigManager(serverConfigManager);
		return service;
	}

	@Bean
	public HostinfoService hostinfoService(HostInfoRepository hostinfoRepository,
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

	private Map<String, StorageBuilder> buildStorageBuilders(StorageBuilder storageSQLBuilder,
			StorageBuilder storageCacheBuilder, StorageBuilder storageRPCBuilder) {
		Map<String, StorageBuilder> builders = new LinkedHashMap<String, StorageBuilder>();

		builders.put(storageSQLBuilder.getType(), storageSQLBuilder);
		builders.put(storageCacheBuilder.getType(), storageCacheBuilder);
		builders.put(storageRPCBuilder.getType(), storageRPCBuilder);
		return builders;
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
