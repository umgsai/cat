package com.dianping.cat.core.mybatis.repository.overload;

import com.dianping.cat.core.mybatis.SpringBackedRepositorySupport;
import com.dianping.cat.core.mybatis.overload.dao.OverloadMapper;
import com.dianping.cat.core.mybatis.overload.dao.data.OverloadDO;
import com.dianping.cat.home.dal.report.Overload;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

public class OverloadRepository extends SpringBackedRepositorySupport<OverloadMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(OverloadRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/OverloadMapper.xml";

	public OverloadRepository() {
		super(OverloadMapper.class, MAPPER_RESOURCE, "OverloadRepository is using Spring managed OverloadMapper.");
	}

	public Overload createLocal() {
		return new Overload();
	}

	public int deleteByPK(Overload proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for Overload.", e);
		}
	}

	public List<Overload> findIdAndSizeByDuration(java.util.Date startTime, java.util.Date endTime) {
		OverloadMapper mapper = springMapper(LOGGER);
		OverloadDO record = new OverloadDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		try {
			return mapper.findIdAndSizeByDuration(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findIdAndSizeByDuration for Overload.", e);
		}
	}

	public Overload findByPK(int keyId) {
		return findByPK((long) keyId);
	}

	public Overload findByPK(long keyId) {
		OverloadMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for Overload.", e);
		}
	}

	public Overload findMaxIdByType(int type) {
		OverloadMapper mapper = springMapper(LOGGER);
		OverloadDO record = new OverloadDO();

		record.setType(type);
		try {
			OverloadDO result = mapper.findMaxIdByType(record).stream().findFirst().orElse(null);

			return requireFound(result, "findMaxIdByType", record.toString());
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findMaxIdByType for Overload.", e);
		}
	}

	public Overload findCount() {
		OverloadMapper mapper = springMapper(LOGGER);
		OverloadDO record = new OverloadDO();

		try {
			OverloadDO result = mapper.findCount(record).stream().findFirst().orElse(null);

			return requireFound(result, "findCount", record.toString());
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findCount for Overload.", e);
		}
	}

	public int insert(Overload proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			OverloadDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for Overload.", e);
		}
	}

	public int updateByPK(Overload proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for Overload.", e);
		}
	}

	private Overload requireFound(OverloadDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No Overload found by " + field + "(" + value + ").", 1);
		}

		return toModel(record);
	}

	private Overload toModel(OverloadDO record) {
		Overload model = new Overload();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getReportId() != null) {
			model.setReportId(record.getReportId());
		}
		if (record.getReportType() != null) {
			model.setReportType(record.getReportType());
		}
		if (record.getReportSize() != null) {
			model.setReportSize(record.getReportSize());
		}
		if (record.getPeriod() != null) {
			model.setPeriod(record.getPeriod());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		if (record.getUpdateTime() != null) {
			model.setUpdateTime(record.getUpdateTime());
		}
		if (record.getMaxId() != null) {
			model.setMaxId(record.getMaxId());
		}
		if (record.getCount() != null) {
			model.setCount(record.getCount());
		}
		model.afterLoad();
		return model;
	}

	private OverloadDO toRecord(Overload model) {
		OverloadDO record = new OverloadDO();

		record.setId(model.getId());
		record.setReportId(model.getReportId());
		record.setReportType(model.getReportType());
		record.setReportSize(model.getReportSize());
		record.setPeriod(model.getPeriod());
		record.setCreateTime(model.getCreateTime());
		record.setUpdateTime(model.getUpdateTime());
		record.setKeyId(model.getKeyId());
		record.setStartTime(model.getStartTime());
		record.setEndTime(model.getEndTime());
		record.setType(model.getType());
		return record;
	}
}
