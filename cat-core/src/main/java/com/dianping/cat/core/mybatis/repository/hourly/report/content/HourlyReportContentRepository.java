package com.dianping.cat.core.mybatis.repository.hourly.report.content;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;
import org.unidal.dal.jdbc.datasource.DataSourceManager;

import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.mybatis.generated.hourly.report.content.dao.HourlyReportContentMapper;
import com.dianping.cat.core.mybatis.generated.hourly.report.content.dao.data.HourlyReportContentDO;
import com.dianping.cat.core.mybatis.repository.SupportingMyBatisRepository;

public class HourlyReportContentRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(HourlyReportContentRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/HourlyReportContentMapper.xml";

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();
	private DataSourceManager m_dataSourceManager;

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	private volatile SqlSessionFactory m_sqlSessionFactory;

	public void setDataSourceManager(DataSourceManager dataSourceManager) {
		m_dataSourceManager = dataSourceManager;
	}

	public HourlyReportContent createLocal() {
		return new HourlyReportContent();
	}

	public int deleteByPK(HourlyReportContent proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyReportId()));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(HourlyReportContentMapper.class).deleteByPrimaryKey(proto.getKeyReportId());

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for HourlyReportContent.", e);
		}
	}

	public List<HourlyReportContent> findOverloadReport(int startId, Readset<HourlyReportContent> readset)
			throws DalException {
		HourlyReportContentMapper mapper = springMapper();
		HourlyReportContentDO record = new HourlyReportContentDO();

		record.setStartId(startId);
		if (mapper != null) {
			return mapper.findOverloadReport(record).stream().map(this::toModel).collect(Collectors.toList());
		}

		try (SqlSession session = openSession()) {
			return session.getMapper(HourlyReportContentMapper.class).findOverloadReport(record).stream()
					.map(this::toModel)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findOverloadReport for HourlyReportContent.", e);
		}
	}

	public HourlyReportContent findByPK(int keyReportId, java.util.Date period, Readset<HourlyReportContent> readset)
			throws DalException {
		HourlyReportContentMapper mapper = springMapper();

		if (mapper != null) {
			return requireFound(mapper.findByPrimaryKey(keyReportId), "primary key", String.valueOf(keyReportId));
		}

		try (SqlSession session = openSession()) {
			HourlyReportContentDO record = session.getMapper(HourlyReportContentMapper.class)
					.findByPrimaryKey(keyReportId);

			return requireFound(record, "primary key", String.valueOf(keyReportId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for HourlyReportContent.", e);
		}
	}

	public int insert(HourlyReportContent proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().insert(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			HourlyReportContentDO record = toRecord(proto);
			int count = session.getMapper(HourlyReportContentMapper.class).insert(record);

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for HourlyReportContent.", e);
		}
	}

	public int updateByPK(HourlyReportContent proto, Updateset<HourlyReportContent> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(HourlyReportContentMapper.class).updateByPrimaryKey(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for HourlyReportContent.", e);
		}
	}

	private SqlSessionFactory getSqlSessionFactory() {
		SqlSessionFactory sqlSessionFactory = m_sqlSessionFactory;

		if (sqlSessionFactory == null) {
			synchronized (this) {
				sqlSessionFactory = m_sqlSessionFactory;

				if (sqlSessionFactory == null) {
					sqlSessionFactory = SupportingMyBatisRepository.newSqlSessionFactory(m_dataSourceManager,
							HourlyReportContentMapper.class, MAPPER_RESOURCE);
					m_sqlSessionFactory = sqlSessionFactory;
				}
			}
		}

		return sqlSessionFactory;
	}

	private SqlSession openSession() {
		return getSqlSessionFactory().openSession(false);
	}

	private HourlyReportContentMapper springMapper() {
		if (m_sqlSessionTemplate == null) {
			return null;
		}
		if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
			LOGGER.info("HourlyReportContentRepository is using Spring managed HourlyReportContentMapper.");
		}
		return m_sqlSessionTemplate.getMapper(HourlyReportContentMapper.class);
	}

	private TransactionTemplate springTransactionTemplate() {
		return m_transactionTemplate;
	}

	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		m_sqlSessionTemplate = sqlSessionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		m_transactionTemplate = transactionTemplate;
	}

	private HourlyReportContent requireFound(HourlyReportContentDO record, String field, String value)
			throws DalNotFoundException {
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
