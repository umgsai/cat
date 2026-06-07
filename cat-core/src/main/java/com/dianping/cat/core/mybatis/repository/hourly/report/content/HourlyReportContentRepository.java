package com.dianping.cat.core.mybatis.repository.hourly.report.content;

import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.hourly.report.content.dao.HourlyReportContentMapper;
import com.dianping.cat.core.mybatis.generated.hourly.report.content.dao.data.HourlyReportContentDO;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class HourlyReportContentRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/HourlyReportContentMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return HourlyReportContentMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public HourlyReportContent createLocal() {
		return new HourlyReportContent();
	}

	public int deleteByPK(HourlyReportContent proto) throws DalException {
		try (SqlSession session = openSession()) {
			HourlyReportContentMapper mapper = session.getMapper(HourlyReportContentMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyReportId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for HourlyReportContent.", e);
		}
	}

	public List<HourlyReportContent> findOverloadReport(int startId, Readset<HourlyReportContent> readset) throws DalException {
		try (SqlSession session = openSession()) {
			HourlyReportContentMapper mapper = session.getMapper(HourlyReportContentMapper.class);
			HourlyReportContentDO record = new HourlyReportContentDO();
			record.setStartId(startId);
			return mapper.findOverloadReport(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findOverloadReport for HourlyReportContent.", e);
		}
	}

	public HourlyReportContent findByPK(int keyReportId, java.util.Date period, Readset<HourlyReportContent> readset) throws DalException {
		try (SqlSession session = openSession()) {
			HourlyReportContentMapper mapper = session.getMapper(HourlyReportContentMapper.class);
			HourlyReportContentDO record = mapper.findByPrimaryKey(keyReportId);
			return requireFound(record, "primary key", String.valueOf(keyReportId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for HourlyReportContent.", e);
		}
	}

	public int insert(HourlyReportContent proto) throws DalException {
		try (SqlSession session = openSession()) {
			HourlyReportContentMapper mapper = session.getMapper(HourlyReportContentMapper.class);
			HourlyReportContentDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for HourlyReportContent.", e);
		}
	}

	public int updateByPK(HourlyReportContent proto, Updateset<HourlyReportContent> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			HourlyReportContentMapper mapper = session.getMapper(HourlyReportContentMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for HourlyReportContent.", e);
		}
	}

	private HourlyReportContent requireFound(HourlyReportContentDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No HourlyReportContent found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private HourlyReportContent toModel(HourlyReportContentDO record) {
		HourlyReportContent model = new HourlyReportContent();

		if (record.getReportId() != null) {
			model.setReportId(record.getReportId());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getPeriod() != null) {
			model.setPeriod(record.getPeriod());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		if (record.getContentLength() != null) {
			model.setContentLength(record.getContentLength().longValue());
		}
		model.afterLoad();
		return model;
	}

	private HourlyReportContentDO toRecord(HourlyReportContent model) {
		HourlyReportContentDO record = new HourlyReportContentDO();

		record.setReportId(model.getReportId());
		record.setContent(model.getContent());
		record.setPeriod(model.getPeriod());
		record.setCreationDate(model.getCreationDate());
		record.setKeyReportId(model.getKeyReportId());
		record.setStartId(model.getStartId());
		record.setCapacity(model.getCapacity());
		return record;
	}
}
