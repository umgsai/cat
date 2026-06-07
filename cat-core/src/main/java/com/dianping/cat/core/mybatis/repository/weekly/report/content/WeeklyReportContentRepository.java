package com.dianping.cat.core.mybatis.repository.weekly.report.content;

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

import com.dianping.cat.core.dal.WeeklyReportContent;
import com.dianping.cat.core.mybatis.generated.weekly.report.content.dao.WeeklyReportContentMapper;
import com.dianping.cat.core.mybatis.generated.weekly.report.content.dao.data.WeeklyReportContentDO;
import com.dianping.cat.core.mybatis.repository.SupportingMyBatisRepository;

public class WeeklyReportContentRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(WeeklyReportContentRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/WeeklyReportContentMapper.xml";

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Inject
	private DataSourceManager m_dataSourceManager;

	private volatile SqlSessionFactory m_sqlSessionFactory;

	public WeeklyReportContent createLocal() {
		return new WeeklyReportContent();
	}

	public int deleteByPK(WeeklyReportContent proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyReportId()));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(WeeklyReportContentMapper.class).deleteByPrimaryKey(proto.getKeyReportId());

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for WeeklyReportContent.", e);
		}
	}

	public List<WeeklyReportContent> findOverloadReport(int startId, Readset<WeeklyReportContent> readset)
			throws DalException {
		WeeklyReportContentMapper mapper = springMapper();
		WeeklyReportContentDO record = new WeeklyReportContentDO();

		record.setStartId(startId);
		if (mapper != null) {
			return mapper.findOverloadReport(record).stream().map(this::toModel).collect(Collectors.toList());
		}

		try (SqlSession session = openSession()) {
			return session.getMapper(WeeklyReportContentMapper.class).findOverloadReport(record).stream()
					.map(this::toModel)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findOverloadReport for WeeklyReportContent.", e);
		}
	}

	public WeeklyReportContent findByPK(int keyReportId, Readset<WeeklyReportContent> readset) throws DalException {
		WeeklyReportContentMapper mapper = springMapper();

		if (mapper != null) {
			return requireFound(mapper.findByPrimaryKey(keyReportId), "primary key", String.valueOf(keyReportId));
		}

		try (SqlSession session = openSession()) {
			WeeklyReportContentDO record = session.getMapper(WeeklyReportContentMapper.class)
					.findByPrimaryKey(keyReportId);

			return requireFound(record, "primary key", String.valueOf(keyReportId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for WeeklyReportContent.", e);
		}
	}

	public int insert(WeeklyReportContent proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().insert(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			WeeklyReportContentDO record = toRecord(proto);
			int count = session.getMapper(WeeklyReportContentMapper.class).insert(record);

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for WeeklyReportContent.", e);
		}
	}

	public int updateByPK(WeeklyReportContent proto, Updateset<WeeklyReportContent> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(WeeklyReportContentMapper.class).updateByPrimaryKey(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for WeeklyReportContent.", e);
		}
	}

	private SqlSessionFactory getSqlSessionFactory() {
		SqlSessionFactory sqlSessionFactory = m_sqlSessionFactory;

		if (sqlSessionFactory == null) {
			synchronized (this) {
				sqlSessionFactory = m_sqlSessionFactory;

				if (sqlSessionFactory == null) {
					sqlSessionFactory = SupportingMyBatisRepository.newSqlSessionFactory(m_dataSourceManager,
							WeeklyReportContentMapper.class, MAPPER_RESOURCE);
					m_sqlSessionFactory = sqlSessionFactory;
				}
			}
		}

		return sqlSessionFactory;
	}

	private SqlSession openSession() {
		return getSqlSessionFactory().openSession(false);
	}

	private WeeklyReportContentMapper springMapper() {
		return SupportingMyBatisRepository.springMapper(WeeklyReportContentMapper.class, LOGGER, SPRING_MAPPER_LOGGED,
				"WeeklyReportContentRepository is using Spring managed WeeklyReportContentMapper.");
	}

	private TransactionTemplate springTransactionTemplate() {
		return SupportingMyBatisRepository.springTransactionTemplate();
	}

	private WeeklyReportContent requireFound(WeeklyReportContentDO record, String field, String value)
			throws DalNotFoundException {
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
