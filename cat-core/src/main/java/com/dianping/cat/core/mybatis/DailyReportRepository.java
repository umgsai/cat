package com.dianping.cat.core.mybatis;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.support.TransactionTemplate;

import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.mybatis.mapper.DailyReportMapper;
import com.dianping.cat.core.mybatis.data.DailyReportDO;

public class DailyReportRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(DailyReportRepository.class);

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	public DailyReport createLocal() {
		return new DailyReport();
	}

	public int deleteByDomainNamePeriod(DailyReport proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteByDomainNamePeriod(proto.getDomain(),
				proto.getName(), proto.getPeriod()));
	}

	public int deleteByPK(DailyReport proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().deleteById(proto.getKeyId()));
	}

	public DailyReport findByDomainNamePeriod(String domain, String name, java.util.Date period) {
		DailyReportMapper mapper = springMapper();

		return requireFound(mapper.findByDomainNamePeriod(domain, name, period), "domain/name/period",
				domain + "/" + name + "/" + period);
	}

	public DailyReport findByPK(long keyId) {
		DailyReportMapper mapper = springMapper();

		return requireFound(mapper.findById(keyId), "id", String.valueOf(keyId));
	}

	public int insert(DailyReport proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		DailyReportDO report = toDailyReportDO(proto);
		int count = transactionTemplate.execute(status -> springMapper().insert(report));

		proto.setId(report.getId());
		proto.setKeyId(report.getId());
		return count;
	}

	public List<DailyReport> queryLatestReportsByDomainName(String domain, String name, int limits) {
		DailyReportMapper mapper = springMapper();

		return mapper.queryLatestReportsByDomainName(domain, name, limits).stream()
				.map(this::toDailyReport)
				.collect(Collectors.toList());
	}

	public int updateByPK(DailyReport proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		return transactionTemplate.execute(status -> springMapper().updateById(toDailyReportDO(proto)));
	}

	private DailyReportMapper springMapper() {
		SqlSessionTemplate sqlSessionTemplate = m_sqlSessionTemplate;

		if (sqlSessionTemplate == null) {
			throw new IllegalStateException("Spring SqlSessionTemplate is not configured for DailyReportMapper.");
		}

		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("DailyReportRepository is using Spring managed DailyReportMapper.");
		}

		return sqlSessionTemplate.getMapper(DailyReportMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		if (m_transactionTemplate == null) {
			throw new IllegalStateException("Spring TransactionTemplate is not configured for DailyReportMapper.");
		}
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
	}

	private DailyReport requireFound(DailyReportDO report, String field, String value) {
		if (report == null) {
			throw new EmptyResultDataAccessException(String.format("No daily report found by %s(%s).", field, value), 1);
		}

		return toDailyReport(report);
	}

	private DailyReport toDailyReport(DailyReportDO reportDO) {
		DailyReport report = new DailyReport();

		report.setId(reportDO.getId());
		report.setName(reportDO.getName());
		report.setIp(reportDO.getIp());
		report.setDomain(reportDO.getDomain());
		report.setPeriod(reportDO.getPeriod());
		report.setType(reportDO.getType());
		report.setCreateTime(reportDO.getCreateTime());
		report.afterLoad();
		return report;
	}

	private DailyReportDO toDailyReportDO(DailyReport report) {
		DailyReportDO reportDO = new DailyReportDO();

		reportDO.setId(report.getKeyId() > 0 ? report.getKeyId() : report.getId());
		reportDO.setName(report.getName());
		reportDO.setIp(report.getIp());
		reportDO.setDomain(report.getDomain());
		reportDO.setPeriod(report.getPeriod());
		reportDO.setType(report.getType());
		reportDO.setCreateTime(report.getCreateTime());
		return reportDO;
	}

}
