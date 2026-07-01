package com.dianping.cat.home.spring;

import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.mybatis.mapper.ConfigMapper;
import com.dianping.cat.mybatis.mapper.AlertMapper;
import com.dianping.cat.mybatis.mapper.AlertSummaryMapper;
import com.dianping.cat.mybatis.mapper.AlterationMapper;
import com.dianping.cat.mybatis.mapper.BaselineMapper;
import com.dianping.cat.mybatis.mapper.BusinessConfigMapper;
import com.dianping.cat.mybatis.mapper.ConfigModificationMapper;
import com.dianping.cat.mybatis.mapper.DailyReportContentMapper;
import com.dianping.cat.mybatis.mapper.HostInfoMapper;
import com.dianping.cat.mybatis.mapper.HourlyReportContentMapper;
import com.dianping.cat.mybatis.mapper.HourlyReportMapper;
import com.dianping.cat.mybatis.mapper.MetricGraphMapper;
import com.dianping.cat.mybatis.mapper.MetricScreenMapper;
import com.dianping.cat.mybatis.mapper.MonthlyReportContentMapper;
import com.dianping.cat.mybatis.mapper.MonthReportMapper;
import com.dianping.cat.mybatis.mapper.OverloadMapper;
import com.dianping.cat.mybatis.mapper.ProjectMapper;
import com.dianping.cat.mybatis.mapper.ServerAlarmRuleMapper;
import com.dianping.cat.mybatis.mapper.TaskMapper;
import com.dianping.cat.mybatis.mapper.TopologyGraphMapper;
import com.dianping.cat.mybatis.mapper.UserDefineRuleMapper;
import com.dianping.cat.mybatis.mapper.WeeklyReportContentMapper;
import com.dianping.cat.mybatis.mapper.WeeklyReportMapper;
import com.dianping.cat.mybatis.mapper.DailyReportMapper;

@Component("catHomeSpringStartupVerifier")
public class CatHomeSpringStartupVerifier {
	private static final Logger LOGGER = LoggerFactory.getLogger(CatHomeSpringStartupVerifier.class);

	private static final List<Class<?>> REQUIRED_MAPPERS = List.of(ConfigMapper.class, DailyReportMapper.class,
			HostInfoMapper.class, HourlyReportMapper.class, WeeklyReportMapper.class, MonthReportMapper.class,
			ProjectMapper.class, DailyReportContentMapper.class, HourlyReportContentMapper.class,
			WeeklyReportContentMapper.class, MonthlyReportContentMapper.class, BusinessConfigMapper.class,
			TaskMapper.class, AlertSummaryMapper.class, ConfigModificationMapper.class, BaselineMapper.class,
			OverloadMapper.class, TopologyGraphMapper.class, MetricGraphMapper.class, MetricScreenMapper.class,
			AlterationMapper.class, AlertMapper.class, ServerAlarmRuleMapper.class, UserDefineRuleMapper.class);

	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	@PostConstruct
	public void verify() {
		for (Class<?> mapperClass : REQUIRED_MAPPERS) {
			sqlSessionTemplate.getMapper(mapperClass);
		}

		LOGGER.info("Verified {} Spring managed MyBatis mappers.", REQUIRED_MAPPERS.size());
	}
}
