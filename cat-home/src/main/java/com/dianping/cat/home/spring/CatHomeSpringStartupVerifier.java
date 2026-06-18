package com.dianping.cat.home.spring;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.core.mybatis.mapper.ConfigMapper;
import com.dianping.cat.core.mybatis.alert.dao.AlertMapper;
import com.dianping.cat.core.mybatis.alert.summary.dao.AlertSummaryMapper;
import com.dianping.cat.core.mybatis.alteration.dao.AlterationMapper;
import com.dianping.cat.core.mybatis.baseline.dao.BaselineMapper;
import com.dianping.cat.core.mybatis.mapper.BusinessConfigMapper;
import com.dianping.cat.core.mybatis.config.modification.dao.ConfigModificationMapper;
import com.dianping.cat.core.mybatis.mapper.DailyReportContentMapper;
import com.dianping.cat.core.mybatis.mapper.HostInfoMapper;
import com.dianping.cat.core.mybatis.mapper.HourlyReportContentMapper;
import com.dianping.cat.core.mybatis.mapper.HourlyReportMapper;
import com.dianping.cat.core.mybatis.metric.graph.dao.MetricGraphMapper;
import com.dianping.cat.core.mybatis.metric.screen.dao.MetricScreenMapper;
import com.dianping.cat.core.mybatis.mapper.MonthlyReportContentMapper;
import com.dianping.cat.core.mybatis.mapper.MonthReportMapper;
import com.dianping.cat.core.mybatis.overload.dao.OverloadMapper;
import com.dianping.cat.core.mybatis.mapper.ProjectMapper;
import com.dianping.cat.core.mybatis.server.alarm.rule.dao.ServerAlarmRuleMapper;
import com.dianping.cat.core.mybatis.mapper.TaskMapper;
import com.dianping.cat.core.mybatis.topologygraph.dao.TopologyGraphMapper;
import com.dianping.cat.core.mybatis.user.define.rule.dao.UserDefineRuleMapper;
import com.dianping.cat.core.mybatis.mapper.WeeklyReportContentMapper;
import com.dianping.cat.core.mybatis.mapper.WeeklyReportMapper;
import com.dianping.cat.core.mybatis.mapper.DailyReportMapper;

public class CatHomeSpringStartupVerifier {
	private static final Logger LOGGER = LoggerFactory.getLogger(CatHomeSpringStartupVerifier.class);

	private static final List<Class<?>> REQUIRED_MAPPERS = List.of(ConfigMapper.class, DailyReportMapper.class,
			HostInfoMapper.class, HourlyReportMapper.class, WeeklyReportMapper.class, MonthReportMapper.class,
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
