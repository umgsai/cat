package com.dianping.cat.home.spring;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.core.config.dao.ConfigMapper;
import com.dianping.cat.core.mybatis.generated.alert.dao.AlertMapper;
import com.dianping.cat.core.mybatis.generated.alert.summary.dao.AlertSummaryMapper;
import com.dianping.cat.core.mybatis.generated.alteration.dao.AlterationMapper;
import com.dianping.cat.core.mybatis.generated.baseline.dao.BaselineMapper;
import com.dianping.cat.core.mybatis.generated.business.config.dao.BusinessConfigMapper;
import com.dianping.cat.core.mybatis.generated.config.modification.dao.ConfigModificationMapper;
import com.dianping.cat.core.mybatis.generated.daily.report.content.dao.DailyReportContentMapper;
import com.dianping.cat.core.mybatis.generated.hostinfo.dao.HostinfoMapper;
import com.dianping.cat.core.mybatis.generated.hourly.report.content.dao.HourlyReportContentMapper;
import com.dianping.cat.core.mybatis.generated.hourlyreport.dao.HourlyreportMapper;
import com.dianping.cat.core.mybatis.generated.metric.graph.dao.MetricGraphMapper;
import com.dianping.cat.core.mybatis.generated.metric.screen.dao.MetricScreenMapper;
import com.dianping.cat.core.mybatis.generated.monthly.report.content.dao.MonthlyReportContentMapper;
import com.dianping.cat.core.mybatis.generated.monthreport.dao.MonthreportMapper;
import com.dianping.cat.core.mybatis.generated.overload.dao.OverloadMapper;
import com.dianping.cat.core.mybatis.generated.project.dao.ProjectMapper;
import com.dianping.cat.core.mybatis.generated.server.alarm.rule.dao.ServerAlarmRuleMapper;
import com.dianping.cat.core.mybatis.generated.task.dao.TaskMapper;
import com.dianping.cat.core.mybatis.generated.topologygraph.dao.TopologyGraphMapper;
import com.dianping.cat.core.mybatis.generated.user.define.rule.dao.UserDefineRuleMapper;
import com.dianping.cat.core.mybatis.generated.weekly.report.content.dao.WeeklyReportContentMapper;
import com.dianping.cat.core.mybatis.generated.weeklyreport.dao.WeeklyreportMapper;
import com.dianping.cat.core.report.daily.dao.DailyReportMapper;

public class CatHomeSpringStartupVerifier {
	private static final Logger LOGGER = LoggerFactory.getLogger(CatHomeSpringStartupVerifier.class);

	private static final List<Class<?>> REQUIRED_MAPPERS = List.of(ConfigMapper.class, DailyReportMapper.class,
			HostinfoMapper.class, HourlyreportMapper.class, WeeklyreportMapper.class, MonthreportMapper.class,
			ProjectMapper.class, DailyReportContentMapper.class, HourlyReportContentMapper.class,
			WeeklyReportContentMapper.class, MonthlyReportContentMapper.class, BusinessConfigMapper.class,
			TaskMapper.class, AlertSummaryMapper.class, ConfigModificationMapper.class, BaselineMapper.class,
			OverloadMapper.class, TopologyGraphMapper.class, MetricGraphMapper.class, MetricScreenMapper.class,
			AlterationMapper.class, AlertMapper.class, ServerAlarmRuleMapper.class, UserDefineRuleMapper.class);

	private final SqlSessionTemplate m_sqlSessionTemplate;

	public CatHomeSpringStartupVerifier(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void verify() {
		for (Class<?> mapperClass : REQUIRED_MAPPERS) {
			m_sqlSessionTemplate.getMapper(mapperClass);
		}

		LOGGER.info("Verified {} Spring managed MyBatis mappers.", REQUIRED_MAPPERS.size());
	}
}
