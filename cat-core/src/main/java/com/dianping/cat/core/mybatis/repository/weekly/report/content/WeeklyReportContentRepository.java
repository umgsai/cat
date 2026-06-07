package com.dianping.cat.core.mybatis.repository.weekly.report.content;

import com.dianping.cat.core.dal.WeeklyReportContent;
import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.weekly.report.content.dao.WeeklyReportContentMapper;
import com.dianping.cat.core.mybatis.generated.weekly.report.content.dao.data.WeeklyReportContentDO;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class WeeklyReportContentRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/WeeklyReportContentMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return WeeklyReportContentMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public WeeklyReportContent createLocal() {
		return new WeeklyReportContent();
	}

	public int deleteByPK(WeeklyReportContent proto) throws DalException {
		try (SqlSession session = openSession()) {
			WeeklyReportContentMapper mapper = session.getMapper(WeeklyReportContentMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyReportId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for WeeklyReportContent.", e);
		}
	}

	public List<WeeklyReportContent> findOverloadReport(int startId, Readset<WeeklyReportContent> readset) throws DalException {
		try (SqlSession session = openSession()) {
			WeeklyReportContentMapper mapper = session.getMapper(WeeklyReportContentMapper.class);
			WeeklyReportContentDO record = new WeeklyReportContentDO();
			record.setStartId(startId);
			return mapper.findOverloadReport(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findOverloadReport for WeeklyReportContent.", e);
		}
	}

	public WeeklyReportContent findByPK(int keyReportId, Readset<WeeklyReportContent> readset) throws DalException {
		try (SqlSession session = openSession()) {
			WeeklyReportContentMapper mapper = session.getMapper(WeeklyReportContentMapper.class);
			WeeklyReportContentDO record = mapper.findByPrimaryKey(keyReportId);
			return requireFound(record, "primary key", String.valueOf(keyReportId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for WeeklyReportContent.", e);
		}
	}

	public int insert(WeeklyReportContent proto) throws DalException {
		try (SqlSession session = openSession()) {
			WeeklyReportContentMapper mapper = session.getMapper(WeeklyReportContentMapper.class);
			WeeklyReportContentDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for WeeklyReportContent.", e);
		}
	}

	public int updateByPK(WeeklyReportContent proto, Updateset<WeeklyReportContent> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			WeeklyReportContentMapper mapper = session.getMapper(WeeklyReportContentMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for WeeklyReportContent.", e);
		}
	}

	private WeeklyReportContent requireFound(WeeklyReportContentDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No WeeklyReportContent found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private WeeklyReportContent toModel(WeeklyReportContentDO record) {
		WeeklyReportContent model = new WeeklyReportContent();

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

	private WeeklyReportContentDO toRecord(WeeklyReportContent model) {
		WeeklyReportContentDO record = new WeeklyReportContentDO();

		record.setReportId(model.getReportId());
		record.setContent(model.getContent());
		record.setCreationDate(model.getCreationDate());
		record.setKeyReportId(model.getKeyReportId());
		record.setCapacity(model.getCapacity());
		record.setStartId(model.getStartId());
		return record;
	}
}
