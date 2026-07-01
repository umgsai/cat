package com.dianping.cat.mybatis;

import com.dianping.cat.mybatis.data.OverloadDO;
import com.dianping.cat.mybatis.mapper.OverloadMapper;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

@Component("overloadRepository")
public class OverloadRepository extends SpringBackedRepositorySupport<OverloadMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(OverloadRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/OverloadMapper.xml";

	public OverloadRepository() {
		super(OverloadMapper.class, MAPPER_RESOURCE, "OverloadRepository is using Spring managed OverloadMapper.");
	}

	public OverloadDO createLocal() {
		return new OverloadDO();
	}

	public int deleteByPK(OverloadDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getId()));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing deleteByPK for Overload.", e);
		}
	}

	public List<OverloadDO> findIdAndSizeByDuration(java.util.Date startTime, java.util.Date endTime) {
		OverloadMapper mapper = springMapper(LOGGER);
		OverloadDO record = new OverloadDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		try {
			return mapper.findIdAndSizeByDuration(record);
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findIdAndSizeByDuration for Overload.", e);
		}
	}

	public OverloadDO findByPK(int id) {
		return findByPK((long) id);
	}

	public OverloadDO findByPK(long id) {
		OverloadMapper mapper = springMapper(LOGGER);

		try {
			return requireFound(mapper.findByPrimaryKey(id), "primary key", String.valueOf(id));
		} catch (EmptyResultDataAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing findByPK for Overload.", e);
		}
	}

	public OverloadDO findMaxIdByType(int type) {
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

	public OverloadDO findCount() {
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

	public int insert(OverloadDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).insert(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing insert for Overload.", e);
		}
	}

	public int updateByPK(OverloadDO proto) {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		try {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(proto));
		} catch (Exception e) {
			throw new IllegalStateException("Error when executing updateByPK for Overload.", e);
		}
	}

	private OverloadDO requireFound(OverloadDO record, String field, String value) {
		if (record == null) {
			throw new EmptyResultDataAccessException("No Overload found by " + field + "(" + value + ").", 1);
		}

		return record;
	}
}
