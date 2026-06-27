package com.dianping.cat.home.spring;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@MapperScan(basePackages = {
		"com.dianping.cat.mybatis.mapper",
		"com.dianping.cat.mybatis.alert.dao",
		"com.dianping.cat.mybatis.server.alarm.rule.dao",
		"com.dianping.cat.mybatis.user.define.rule.dao"
})
public class CatHomeDatabaseConfiguration {
	private static final Logger LOGGER = LoggerFactory.getLogger(CatHomeDatabaseConfiguration.class);

	private static final String[] MAPPER_LOCATIONS = {
			"classpath:mybatis/mapper/ConfigMapper.xml",
			"classpath:mybatis/mapper/DailyReportMapper.xml",
			"classpath:mybatis/mapper/HostInfoMapper.xml",
			"classpath:mybatis/mapper/HourlyReportMapper.xml",
			"classpath:mybatis/mapper/WeeklyReportMapper.xml",
			"classpath:mybatis/mapper/MonthReportMapper.xml",
			"classpath:mybatis/mapper/ProjectMapper.xml",
			"classpath:mybatis/mapper/DailyReportContentMapper.xml",
			"classpath:mybatis/mapper/HourlyReportContentMapper.xml",
			"classpath:mybatis/mapper/WeeklyReportContentMapper.xml",
			"classpath:mybatis/mapper/MonthlyReportContentMapper.xml",
			"classpath:mybatis/mapper/BusinessConfigMapper.xml",
			"classpath:mybatis/mapper/TaskMapper.xml",
			"classpath:mybatis/mapper/AlertSummaryMapper.xml",
			"classpath:mybatis/mapper/ConfigModificationMapper.xml",
			"classpath:mybatis/mapper/BaselineMapper.xml",
			"classpath:mybatis/mapper/OverloadMapper.xml",
			"classpath:mybatis/mapper/TopologyGraphMapper.xml",
			"classpath:mybatis/mapper/MetricGraphMapper.xml",
			"classpath:mybatis/mapper/MetricScreenMapper.xml",
			"classpath:mybatis/mapper/AlterationMapper.xml",
			"classpath:mybatis/mapper/AlertMapper.xml",
			"classpath:mybatis/mapper/ServerAlarmRuleMapper.xml",
			"classpath:mybatis/mapper/UserDefineRuleMapper.xml"
	};

	@Bean
	public DataSource catDataSource() {
		LOGGER.info("Creating CAT datasource.");
		return CatHomeSpringDataSourceFactory.createCatDataSource();
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource catDataSource) throws Exception {
		LOGGER.info("Creating CAT SqlSessionFactory.");

		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

		factory.setDataSource(catDataSource);
		factory.setMapperLocations(resolveMapperResources(resolver));

		return factory.getObject();
	}

	@Bean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		LOGGER.info("Creating CAT SqlSessionTemplate.");
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	@Bean
	public PlatformTransactionManager transactionManager(DataSource catDataSource) {
		LOGGER.info("Creating CAT transaction manager.");
		return new DataSourceTransactionManager(catDataSource);
	}

	@Bean
	public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
		LOGGER.info("Creating CAT transaction template.");
		return new TransactionTemplate(transactionManager);
	}

	private Resource[] resolveMapperResources(PathMatchingResourcePatternResolver resolver) {
		Resource[] resources = new Resource[MAPPER_LOCATIONS.length];

		for (int i = 0; i < MAPPER_LOCATIONS.length; i++) {
			resources[i] = resolver.getResource(MAPPER_LOCATIONS[i]);
		}

		LOGGER.info("Configured {} MyBatis mapper resources.", resources.length);
		return resources;
	}
}
