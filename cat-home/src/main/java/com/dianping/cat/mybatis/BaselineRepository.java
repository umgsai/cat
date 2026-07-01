package com.dianping.cat.mybatis;

import com.dianping.cat.mybatis.data.BaselineDO;
import com.dianping.cat.mybatis.mapper.BaselineMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

@Component("baselineRepository")
public class BaselineRepository extends SpringBackedRepositorySupport<BaselineMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(BaselineRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/BaselineMapper.xml";

	public BaselineRepository() {
		super(BaselineMapper.class, MAPPER_RESOURCE, "BaselineRepository is using Spring managed BaselineMapper.");
	}

	public BaselineDO createLocal() {
		return new BaselineDO();
	}

	public int deleteByPK(BaselineDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for Baseline.", e);
		}
	}

	public BaselineDO findByPK(int id) {
		return findByPK((long) id);
	}

	public BaselineDO findByPK(long id) {
		BaselineMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(id), "primary key", String.valueOf(id));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for Baseline.", e);
		}
	}

	public BaselineDO findByReportNameKeyTime(java.util.Date reportPeriod, String reportName, String indexKey) {
		BaselineMapper mapper = springMapper(LOGGER);
		BaselineDO record = new BaselineDO();

		record.setReportPeriod(reportPeriod);
		record.setReportName(reportName);
		record.setIndexKey(indexKey);
		try {
			BaselineDO result = mapper.findByReportNameKeyTime(record).stream().findFirst().orElse(null);

			return requireFound(result, "findByReportNameKeyTime", record.toString());
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByReportNameKeyTime for Baseline.", e);
		}
	}

	public int insert(BaselineDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).insert(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for Baseline.", e);
		}
	}

	public int updateByPK(BaselineDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for Baseline.", e);
		}
	}

	private BaselineDO requireFound(BaselineDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No Baseline found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
