package com.dianping.cat.core.mybatis.repository.overload;

import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.overload.dao.OverloadMapper;
import com.dianping.cat.core.mybatis.generated.overload.dao.data.OverloadDO;
import com.dianping.cat.home.dal.report.Overload;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class OverloadRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/OverloadMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return OverloadMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public Overload createLocal() {
		return new Overload();
	}

	public int deleteByPK(Overload proto) throws DalException {
		try (SqlSession session = openSession()) {
			OverloadMapper mapper = session.getMapper(OverloadMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for Overload.", e);
		}
	}

	public List<Overload> findIdAndSizeByDuration(java.util.Date startTime, java.util.Date endTime, Readset<Overload> readset) throws DalException {
		try (SqlSession session = openSession()) {
			OverloadMapper mapper = session.getMapper(OverloadMapper.class);
			OverloadDO record = new OverloadDO();
			record.setStartTime(startTime);
			record.setEndTime(endTime);
			return mapper.findIdAndSizeByDuration(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findIdAndSizeByDuration for Overload.", e);
		}
	}

	public Overload findByPK(int keyId, Readset<Overload> readset) throws DalException {
		try (SqlSession session = openSession()) {
			OverloadMapper mapper = session.getMapper(OverloadMapper.class);
			OverloadDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for Overload.", e);
		}
	}

	public Overload findMaxIdByType(int type, Readset<Overload> readset) throws DalException {
		try (SqlSession session = openSession()) {
			OverloadMapper mapper = session.getMapper(OverloadMapper.class);
			OverloadDO record = new OverloadDO();
			record.setType(type);
			OverloadDO result = mapper.findMaxIdByType(record).stream().findFirst().orElse(null);
			return requireFound(result, "findMaxIdByType", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findMaxIdByType for Overload.", e);
		}
	}

	public Overload findCount(Readset<Overload> readset) throws DalException {
		try (SqlSession session = openSession()) {
			OverloadMapper mapper = session.getMapper(OverloadMapper.class);
			OverloadDO record = new OverloadDO();
			OverloadDO result = mapper.findCount(record).stream().findFirst().orElse(null);
			return requireFound(result, "findCount", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findCount for Overload.", e);
		}
	}

	public int insert(Overload proto) throws DalException {
		try (SqlSession session = openSession()) {
			OverloadMapper mapper = session.getMapper(OverloadMapper.class);
			OverloadDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for Overload.", e);
		}
	}

	public int updateByPK(Overload proto, Updateset<Overload> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			OverloadMapper mapper = session.getMapper(OverloadMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for Overload.", e);
		}
	}

	private Overload requireFound(OverloadDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No Overload found by " + field + "(" + value + ").");
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
		record.setCreationDate(model.getCreationDate());
		record.setKeyId(model.getKeyId());
		record.setStartTime(model.getStartTime());
		record.setEndTime(model.getEndTime());
		record.setType(model.getType());
		return record;
	}
}
