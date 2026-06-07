package com.dianping.cat.core.mybatis.repository.daily.report.content;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.mybatis.generated.daily.report.content.dao.DailyReportContentMapper;
import com.dianping.cat.core.mybatis.generated.daily.report.content.dao.data.DailyReportContentDO;
import com.dianping.cat.core.mybatis.repository.SupportingMyBatisRepository;

public class DailyReportContentRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(DailyReportContentRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/DailyReportContentMapper.xml";

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Inject
	private DataSourceManager m_dataSourceManager;

	private volatile SqlSessionFactory m_sqlSessionFactory;

	public DailyReportContent createLocal() {
		return new DailyReportContent();
	}

	public int deleteByPK(DailyReportContent proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyReportId()));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(DailyReportContentMapper.class).deleteByPrimaryKey(proto.getKeyReportId());

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for DailyReportContent.", e);
		}
	}

	public List<DailyReportContent> findOverloadReport(int startId, Readset<DailyReportContent> readset)
			throws DalException {
		DailyReportContentMapper mapper = springMapper();
		DailyReportContentDO record = new DailyReportContentDO();

		record.setStartId(startId);
		if (mapper != null) {
			return mapper.findOverloadReport(record).stream().map(this::toModel).collect(Collectors.toList());
		}

		try (SqlSession session = openSession()) {
			return session.getMapper(DailyReportContentMapper.class).findOverloadReport(record).stream()
					.map(this::toModel)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findOverloadReport for DailyReportContent.", e);
		}
	}

	public DailyReportContent findByPK(int keyReportId, Readset<DailyReportContent> readset) throws DalException {
		DailyReportContentMapper mapper = springMapper();

		if (mapper != null) {
			return requireFound(mapper.findByPrimaryKey(keyReportId), "primary key", String.valueOf(keyReportId));
		}

		try (SqlSession session = openSession()) {
			DailyReportContentDO record = session.getMapper(DailyReportContentMapper.class).findByPrimaryKey(keyReportId);

			return requireFound(record, "primary key", String.valueOf(keyReportId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for DailyReportContent.", e);
		}
	}

	public int insert(DailyReportContent proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().insert(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			DailyReportContentDO record = toRecord(proto);
			int count = session.getMapper(DailyReportContentMapper.class).insert(record);

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for DailyReportContent.", e);
		}
	}

	public int updateByPK(DailyReportContent proto, Updateset<DailyReportContent> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(DailyReportContentMapper.class).updateByPrimaryKey(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for DailyReportContent.", e);
		}
	}

	private SqlSessionFactory getSqlSessionFactory() {
		SqlSessionFactory sqlSessionFactory = m_sqlSessionFactory;

		if (sqlSessionFactory == null) {
			synchronized (this) {
				sqlSessionFactory = m_sqlSessionFactory;

				if (sqlSessionFactory == null) {
					sqlSessionFactory = SupportingMyBatisRepository.newSqlSessionFactory(m_dataSourceManager,
							DailyReportContentMapper.class, MAPPER_RESOURCE);
					m_sqlSessionFactory = sqlSessionFactory;
				}
			}
		}

		return sqlSessionFactory;
	}

	private SqlSession openSession() {
		return getSqlSessionFactory().openSession(false);
	}

	private DailyReportContentMapper springMapper() {
		return SupportingMyBatisRepository.springMapper(DailyReportContentMapper.class, LOGGER, SPRING_MAPPER_LOGGED,
				"DailyReportContentRepository is using Spring managed DailyReportContentMapper.");
	}

	private TransactionTemplate springTransactionTemplate() {
		return SupportingMyBatisRepository.springTransactionTemplate();
	}

	private DailyReportContent requireFound(DailyReportContentDO record, String field, String value)
			throws DalNotFoundException {
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
