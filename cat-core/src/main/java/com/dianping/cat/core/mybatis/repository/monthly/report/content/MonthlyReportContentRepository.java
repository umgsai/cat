package com.dianping.cat.core.mybatis.repository.monthly.report.content;

import com.dianping.cat.core.dal.MonthlyReportContent;
import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.monthly.report.content.dao.MonthlyReportContentMapper;
import com.dianping.cat.core.mybatis.generated.monthly.report.content.dao.data.MonthlyReportContentDO;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class MonthlyReportContentRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/MonthlyReportContentMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return MonthlyReportContentMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public MonthlyReportContent createLocal() {
		return new MonthlyReportContent();
	}

	public int deleteByPK(MonthlyReportContent proto) throws DalException {
		try (SqlSession session = openSession()) {
			MonthlyReportContentMapper mapper = session.getMapper(MonthlyReportContentMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyReportId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for MonthlyReportContent.", e);
		}
	}

	public List<MonthlyReportContent> findOverloadReport(int startId, Readset<MonthlyReportContent> readset) throws DalException {
		try (SqlSession session = openSession()) {
			MonthlyReportContentMapper mapper = session.getMapper(MonthlyReportContentMapper.class);
			MonthlyReportContentDO record = new MonthlyReportContentDO();
			record.setStartId(startId);
			return mapper.findOverloadReport(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findOverloadReport for MonthlyReportContent.", e);
		}
	}

	public MonthlyReportContent findByPK(int keyReportId, Readset<MonthlyReportContent> readset) throws DalException {
		try (SqlSession session = openSession()) {
			MonthlyReportContentMapper mapper = session.getMapper(MonthlyReportContentMapper.class);
			MonthlyReportContentDO record = mapper.findByPrimaryKey(keyReportId);
			return requireFound(record, "primary key", String.valueOf(keyReportId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for MonthlyReportContent.", e);
		}
	}

	public int insert(MonthlyReportContent proto) throws DalException {
		try (SqlSession session = openSession()) {
			MonthlyReportContentMapper mapper = session.getMapper(MonthlyReportContentMapper.class);
			MonthlyReportContentDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for MonthlyReportContent.", e);
		}
	}

	public int updateByPK(MonthlyReportContent proto, Updateset<MonthlyReportContent> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			MonthlyReportContentMapper mapper = session.getMapper(MonthlyReportContentMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for MonthlyReportContent.", e);
		}
	}

	private MonthlyReportContent requireFound(MonthlyReportContentDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No MonthlyReportContent found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private MonthlyReportContent toModel(MonthlyReportContentDO record) {
		MonthlyReportContent model = new MonthlyReportContent();

		if (record.getReportId() != null) {
			model.setReportId(record.getReportId());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		if (record.getContentLength() != null) {
			model.setContentLength(record.getContentLength());
		}
		model.afterLoad();
		return model;
	}

	private MonthlyReportContentDO toRecord(MonthlyReportContent model) {
		MonthlyReportContentDO record = new MonthlyReportContentDO();

		record.setReportId(model.getReportId());
		record.setContent(model.getContent());
		record.setCreationDate(model.getCreationDate());
		record.setKeyReportId(model.getKeyReportId());
		record.setCapacity(model.getCapacity());
		record.setStartId(model.getStartId());
		return record;
	}
}
