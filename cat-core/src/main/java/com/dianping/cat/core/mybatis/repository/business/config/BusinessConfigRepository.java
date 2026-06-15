package com.dianping.cat.core.mybatis.repository.business.config;

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

import com.dianping.cat.core.config.BusinessConfig;
import com.dianping.cat.core.mybatis.generated.business.config.dao.BusinessConfigMapper;
import com.dianping.cat.core.mybatis.generated.business.config.dao.data.BusinessConfigDO;
import com.dianping.cat.core.mybatis.repository.SupportingMyBatisRepository;

public class BusinessConfigRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessConfigRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/BusinessConfigMapper.xml";

	private static final AtomicBoolean SPRING_MAPPER_LOGGED = new AtomicBoolean();

	private DataSourceManager m_dataSourceManager;

	private SqlSessionTemplate m_sqlSessionTemplate;

	private TransactionTemplate m_transactionTemplate;

	private volatile SqlSessionFactory m_sqlSessionFactory;

	public BusinessConfig createLocal() {
		return new BusinessConfig();
	}

	public int deleteByPK(BusinessConfig proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().deleteByPrimaryKey(proto.getKeyId()));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(BusinessConfigMapper.class).deleteByPrimaryKey(proto.getKeyId());

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for BusinessConfig.", e);
		}
	}

	public List<BusinessConfig> findByName(String name, Readset<BusinessConfig> readset) throws DalException {
		BusinessConfigMapper mapper = springMapper();
		BusinessConfigDO record = new BusinessConfigDO();

		record.setName(name);
		if (mapper != null) {
			return mapper.findByName(record).stream().map(this::toModel).collect(Collectors.toList());
		}

		try (SqlSession session = openSession()) {
			return session.getMapper(BusinessConfigMapper.class).findByName(record).stream()
					.map(this::toModel)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findByName for BusinessConfig.", e);
		}
	}

	public BusinessConfig findByPK(int keyId, Readset<BusinessConfig> readset) throws DalException {
		BusinessConfigMapper mapper = springMapper();

		if (mapper != null) {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		}

		try (SqlSession session = openSession()) {
			BusinessConfigDO record = session.getMapper(BusinessConfigMapper.class).findByPrimaryKey(keyId);

			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for BusinessConfig.", e);
		}
	}

	public BusinessConfig findByNameDomain(String name, String domain, Readset<BusinessConfig> readset)
			throws DalException {
		BusinessConfigMapper mapper = springMapper();
		BusinessConfigDO record = new BusinessConfigDO();

		record.setName(name);
		record.setDomain(domain);
		if (mapper != null) {
			BusinessConfigDO result = mapper.findByNameDomain(record).stream().findFirst().orElse(null);

			return requireFound(result, "findByNameDomain", record.toString());
		}

		try (SqlSession session = openSession()) {
			BusinessConfigDO result = session.getMapper(BusinessConfigMapper.class).findByNameDomain(record).stream()
					.findFirst()
					.orElse(null);

			return requireFound(result, "findByNameDomain", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByNameDomain for BusinessConfig.", e);
		}
	}

	public int insert(BusinessConfig proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			BusinessConfigDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper().insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		}

		try (SqlSession session = openSession()) {
			BusinessConfigDO record = toRecord(proto);
			int count = session.getMapper(BusinessConfigMapper.class).insert(record);

			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for BusinessConfig.", e);
		}
	}

	public int updateByPK(BusinessConfig proto, Updateset<BusinessConfig> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().updateByPrimaryKey(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(BusinessConfigMapper.class).updateByPrimaryKey(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for BusinessConfig.", e);
		}
	}

	public int updateBaseConfigByDomain(BusinessConfig proto, Updateset<BusinessConfig> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper().updateBaseConfigByDomain(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(BusinessConfigMapper.class).updateBaseConfigByDomain(toRecord(proto));

			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateBaseConfigByDomain for BusinessConfig.", e);
		}
	}

	private SqlSessionFactory getSqlSessionFactory() {
		SqlSessionFactory sqlSessionFactory = m_sqlSessionFactory;

		if (sqlSessionFactory == null) {
			synchronized (this) {
				sqlSessionFactory = m_sqlSessionFactory;

				if (sqlSessionFactory == null) {
					sqlSessionFactory = SupportingMyBatisRepository.newSqlSessionFactory(m_dataSourceManager,
							BusinessConfigMapper.class, MAPPER_RESOURCE);
					m_sqlSessionFactory = sqlSessionFactory;
				}
			}
		}

		return sqlSessionFactory;
	}

	private SqlSession openSession() {
		return getSqlSessionFactory().openSession(false);
	}

	private BusinessConfigMapper springMapper() {
		if (m_sqlSessionTemplate != null) {
			if (SPRING_MAPPER_LOGGED.compareAndSet(false, true)) {
				LOGGER.info("BusinessConfigRepository is using Spring managed BusinessConfigMapper.");
			}

			return m_sqlSessionTemplate.getMapper(BusinessConfigMapper.class);
		}

		return null;
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

	private BusinessConfig requireFound(BusinessConfigDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No BusinessConfig found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private BusinessConfig toModel(BusinessConfigDO record) {
		BusinessConfig model = new BusinessConfig();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getName() != null) {
			model.setName(record.getName());
		}
		if (record.getDomain() != null) {
			model.setDomain(record.getDomain());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getUpdatetime() != null) {
			model.setUpdatetime(record.getUpdatetime());
		}
		model.afterLoad();
		return model;
	}

	private BusinessConfigDO toRecord(BusinessConfig model) {
		BusinessConfigDO record = new BusinessConfigDO();

		record.setId(model.getId());
		record.setName(model.getName());
		record.setDomain(model.getDomain());
		record.setContent(model.getContent());
		record.setUpdatetime(model.getUpdatetime());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
