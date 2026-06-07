package com.dianping.cat.core.mybatis.repository.monthly.report.content;

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

import com.dianping.cat.core.dal.MonthlyReportContent;
import com.dianping.cat.core.mybatis.generated.monthly.report.content.dao.MonthlyReportContentMapper;
import com.dianping.cat.core.mybatis.generated.monthly.report.content.dao.data.MonthlyReportContentDO;
import com.dianping.cat.core.mybatis.repository.SupportingMyBatisRepository;

public class MonthlyReportContentRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyReportContentRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/MonthlyReportContentMapper.xml";

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	@Inject
	private DataSourceManager m_dataSourceManager;

	private volatile SqlSessionFactory m_sqlSessionFactory;

	public MonthlyReportContent createLocal() {
		return new MonthlyReportContent();
	}

	public int deleteByPK(MonthlyReportContent proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyReportId()));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(MonthlyReportContentMapper.class).deleteByPrimaryKey(proto.getKeyReportId());

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for MonthlyReportContent.", e);
		}
	}

	public List<MonthlyReportContent> findOverloadReport(int startId, Readset<MonthlyReportContent> readset)
			throws DalException {
		MonthlyReportContentMapper mapper = springMapper();
		MonthlyReportContentDO record = new MonthlyReportContentDO();

		record.setStartId(startId);
		if (mapper != null) {
			return mapper.findOverloadReport(record).stream().map(this::toModel).collect(Collectors.toList());
		}

		try (SqlSession session = openSession()) {
			return session.getMapper(MonthlyReportContentMapper.class).findOverloadReport(record).stream()
					.map(this::toModel)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findOverloadReport for MonthlyReportContent.", e);
		}
	}

	public MonthlyReportContent findByPK(int keyReportId, Readset<MonthlyReportContent> readset) throws DalException {
		MonthlyReportContentMapper mapper = springMapper();

		if (mapper != null) {
			return requireFound(mapper.findByPrimaryKey(keyReportId), "primary key", String.valueOf(keyReportId));
		}

		try (SqlSession session = openSession()) {
			MonthlyReportContentDO record = session.getMapper(MonthlyReportContentMapper.class)
					.findByPrimaryKey(keyReportId);

			return requireFound(record, "primary key", String.valueOf(keyReportId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for MonthlyReportContent.", e);
		}
	}

	public int insert(MonthlyReportContent proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().insert(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			MonthlyReportContentDO record = toRecord(proto);
			int count = session.getMapper(MonthlyReportContentMapper.class).insert(record);

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for MonthlyReportContent.", e);
		}
	}

	public int updateByPK(MonthlyReportContent proto, Updateset<MonthlyReportContent> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(MonthlyReportContentMapper.class).updateByPrimaryKey(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for MonthlyReportContent.", e);
		}
	}

	private SqlSessionFactory getSqlSessionFactory() {
		SqlSessionFactory sqlSessionFactory = m_sqlSessionFactory;

		if (sqlSessionFactory == null) {
			synchronized (this) {
				sqlSessionFactory = m_sqlSessionFactory;

				if (sqlSessionFactory == null) {
					sqlSessionFactory = SupportingMyBatisRepository.newSqlSessionFactory(m_dataSourceManager,
							MonthlyReportContentMapper.class, MAPPER_RESOURCE);
					m_sqlSessionFactory = sqlSessionFactory;
				}
			}
		}

		return sqlSessionFactory;
	}

	private SqlSession openSession() {
		return getSqlSessionFactory().openSession(false);
	}

	private MonthlyReportContentMapper springMapper() {
		return SupportingMyBatisRepository.springMapper(MonthlyReportContentMapper.class, LOGGER, SPRING_MAPPER_LOGGED,
				"MonthlyReportContentRepository is using Spring managed MonthlyReportContentMapper.");
	}

	private TransactionTemplate springTransactionTemplate() {
		return SupportingMyBatisRepository.springTransactionTemplate();
	}

	private MonthlyReportContent requireFound(MonthlyReportContentDO record, String field, String value)
			throws DalNotFoundException {
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
