package com.dianping.cat.mybatis;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.mybatis.data.DailyReportDO;
import com.dianping.cat.mybatis.mapper.DailyReportMapper;

@Component("dailyReportRepository")
public class DailyReportRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(DailyReportRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

	@Resource(name = "transactionTemplate")
	private TransactionTemplate transactionTemplate;

	public DailyReportDO createLocal() {
		return new DailyReportDO();
	}

	public int deleteByDomainNamePeriod(DailyReportDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteByDomainNamePeriod(proto.getDomain(),
				proto.getName(), proto.getPeriod()));
	}

	public int deleteByPK(DailyReportDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteById(proto.getId()));
	}

	public DailyReportDO findByDomainNamePeriod(String domain, String name, java.util.Date period) {
		DailyReportMapper mapper = springMapper();

		return requireFound(mapper.findByDomainNamePeriod(domain, name, period), "domain/name/period",
				domain + "/" + name + "/" + period);
	}

	public DailyReportDO findDOByDomainNamePeriod(String domain, String name, java.util.Date period) {
		return findByDomainNamePeriod(domain, name, period);
	}

	public DailyReportDO findByPK(long keyId) {
		DailyReportMapper mapper = springMapper();

		return requireFound(mapper.findById(keyId), "id", String.valueOf(keyId));
	}

	public DailyReportDO findDOByPK(long keyId) {
		return findByPK(keyId);
	}

	public int insert(DailyReportDO report) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		int count = transactionTemplate.execute(status -> springMapper().insert(report));

		return count;
	}

	public List<DailyReportDO> queryLatestReportsByDomainName(String domain, String name, int limits) {
		return springMapper().queryLatestReportsByDomainName(domain, name, limits);
	}

	public List<DailyReportDO> queryLatestDOReportsByDomainName(String domain, String name, int limits) {
		return queryLatestReportsByDomainName(domain, name, limits);
	}

	public int updateByPK(DailyReportDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateById(proto));
	}

	private DailyReportMapper springMapper() {
		SqlSessionTemplate template = sqlSessionTemplate;

		if (template == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for DailyReportMapper.");
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("DailyReportRepository is using Spring managed DailyReportMapper.");
		}

		return template.getMapper(DailyReportMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for DailyReportMapper.");
		}
		return transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	private DailyReportDO requireFound(DailyReportDO report, String field, String value) {
		if (report == null) {
			throw new EmptyResultDataAccessException(String.format("No daily report found by %s(%s).", field, value), 1);
		}

		return report;
	}

}
