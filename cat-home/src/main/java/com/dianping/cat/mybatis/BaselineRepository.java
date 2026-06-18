package com.dianping.cat.mybatis;

import com.dianping.cat.mybatis.data.BaselineDO;
import com.dianping.cat.mybatis.mapper.BaselineMapper;
import com.dianping.cat.home.dal.report.Baseline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

public class BaselineRepository extends SpringBackedRepositorySupport<BaselineMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(BaselineRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/BaselineMapper.xml";

	public BaselineRepository() {
		super(BaselineMapper.class, MAPPER_RESOURCE, "BaselineRepository is using Spring managed BaselineMapper.");
	}

	public Baseline createLocal() {
		return new Baseline();
	}

	public int deleteByPK(Baseline proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for Baseline.", e);
		}
	}

	public Baseline findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public Baseline findByPK(long keyId) {
		BaselineMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for Baseline.", e);
		}
	}

	public Baseline findByReportNameKeyTime(java.util.Date reportPeriod, String reportName, String indexKey) {
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

	public int insert(Baseline proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			BaselineDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for Baseline.", e);
		}
	}

	public int updateByPK(Baseline proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for Baseline.", e);
		}
	}

	private Baseline requireFound(BaselineDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No Baseline found by " + field + "(" + value + ").", 1);
		}

		return toModel(record);
	}

	private Baseline toModel(BaselineDO record) {
		Baseline model = new Baseline();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getReportName() != null) {
			model.setReportName(record.getReportName());
		}
		if (record.getIndexKey() != null) {
			model.setIndexKey(record.getIndexKey());
		}
		if (record.getReportPeriod() != null) {
			model.setReportPeriod(record.getReportPeriod());
		}
		if (record.getData() != null) {
			model.setData(record.getData());
		}
		if (record.getCreateTime() != null) {
			model.setCreateTime(record.getCreateTime());
		}
		if (record.getUpdateTime() != null) {
			model.setUpdateTime(record.getUpdateTime());
		}
		model.afterLoad();
		return model;
	}

	private BaselineDO toRecord(Baseline model) {
		BaselineDO record = new BaselineDO();

		record.setId(model.getId());
		record.setReportName(model.getReportName());
		record.setIndexKey(model.getIndexKey());
		record.setReportPeriod(model.getReportPeriod());
		record.setData(model.getData());
		record.setCreateTime(model.getCreateTime());
		record.setUpdateTime(model.getUpdateTime());
		record.setKeyId(model.getKeyId());
		record.setDataInDoubleArray(model.getDataInDoubleArray());
		return record;
	}
}
