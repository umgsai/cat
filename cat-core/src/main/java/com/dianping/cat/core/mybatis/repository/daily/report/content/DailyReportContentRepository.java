package com.dianping.cat.core.mybatis.repository.daily.report.content;

import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.daily.report.content.dao.DailyReportContentMapper;
import com.dianping.cat.core.mybatis.generated.daily.report.content.dao.data.DailyReportContentDO;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class DailyReportContentRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/DailyReportContentMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return DailyReportContentMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public DailyReportContent createLocal() {
		return new DailyReportContent();
	}

	public int deleteByPK(DailyReportContent proto) throws DalException {
		try (SqlSession session = openSession()) {
			DailyReportContentMapper mapper = session.getMapper(DailyReportContentMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyReportId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for DailyReportContent.", e);
		}
	}

	public List<DailyReportContent> findOverloadReport(int startId, Readset<DailyReportContent> readset) throws DalException {
		try (SqlSession session = openSession()) {
			DailyReportContentMapper mapper = session.getMapper(DailyReportContentMapper.class);
			DailyReportContentDO record = new DailyReportContentDO();
			record.setStartId(startId);
			return mapper.findOverloadReport(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findOverloadReport for DailyReportContent.", e);
		}
	}

	public DailyReportContent findByPK(int keyReportId, Readset<DailyReportContent> readset) throws DalException {
		try (SqlSession session = openSession()) {
			DailyReportContentMapper mapper = session.getMapper(DailyReportContentMapper.class);
			DailyReportContentDO record = mapper.findByPrimaryKey(keyReportId);
			return requireFound(record, "primary key", String.valueOf(keyReportId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for DailyReportContent.", e);
		}
	}

	public int insert(DailyReportContent proto) throws DalException {
		try (SqlSession session = openSession()) {
			DailyReportContentMapper mapper = session.getMapper(DailyReportContentMapper.class);
			DailyReportContentDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for DailyReportContent.", e);
		}
	}

	public int updateByPK(DailyReportContent proto, Updateset<DailyReportContent> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			DailyReportContentMapper mapper = session.getMapper(DailyReportContentMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for DailyReportContent.", e);
		}
	}

	private DailyReportContent requireFound(DailyReportContentDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No DailyReportContent found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private DailyReportContent toModel(DailyReportContentDO record) {
		DailyReportContent model = new DailyReportContent();

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

	private DailyReportContentDO toRecord(DailyReportContent model) {
		DailyReportContentDO record = new DailyReportContentDO();

		record.setReportId(model.getReportId());
		record.setContent(model.getContent());
		record.setCreationDate(model.getCreationDate());
		record.setKeyReportId(model.getKeyReportId());
		record.setStartId(model.getStartId());
		record.setEndId(model.getEndId());
		record.setCapacity(model.getCapacity());
		return record;
	}
}
