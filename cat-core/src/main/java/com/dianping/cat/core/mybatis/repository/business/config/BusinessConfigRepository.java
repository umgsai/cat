package com.dianping.cat.core.mybatis.repository.business.config;

import com.dianping.cat.core.config.BusinessConfig;
import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.business.config.dao.BusinessConfigMapper;
import com.dianping.cat.core.mybatis.generated.business.config.dao.data.BusinessConfigDO;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class BusinessConfigRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/BusinessConfigMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return BusinessConfigMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public BusinessConfig createLocal() {
		return new BusinessConfig();
	}

	public int deleteByPK(BusinessConfig proto) throws DalException {
		try (SqlSession session = openSession()) {
			BusinessConfigMapper mapper = session.getMapper(BusinessConfigMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for BusinessConfig.", e);
		}
	}

	public List<BusinessConfig> findByName(String name, Readset<BusinessConfig> readset) throws DalException {
		try (SqlSession session = openSession()) {
			BusinessConfigMapper mapper = session.getMapper(BusinessConfigMapper.class);
			BusinessConfigDO record = new BusinessConfigDO();
			record.setName(name);
			return mapper.findByName(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findByName for BusinessConfig.", e);
		}
	}

	public BusinessConfig findByPK(int keyId, Readset<BusinessConfig> readset) throws DalException {
		try (SqlSession session = openSession()) {
			BusinessConfigMapper mapper = session.getMapper(BusinessConfigMapper.class);
			BusinessConfigDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for BusinessConfig.", e);
		}
	}

	public BusinessConfig findByNameDomain(String name, String domain, Readset<BusinessConfig> readset) throws DalException {
		try (SqlSession session = openSession()) {
			BusinessConfigMapper mapper = session.getMapper(BusinessConfigMapper.class);
			BusinessConfigDO record = new BusinessConfigDO();
			record.setName(name);
			record.setDomain(domain);
			BusinessConfigDO result = mapper.findByNameDomain(record).stream().findFirst().orElse(null);
			return requireFound(result, "findByNameDomain", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByNameDomain for BusinessConfig.", e);
		}
	}

	public int insert(BusinessConfig proto) throws DalException {
		try (SqlSession session = openSession()) {
			BusinessConfigMapper mapper = session.getMapper(BusinessConfigMapper.class);
			BusinessConfigDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for BusinessConfig.", e);
		}
	}

	public int updateByPK(BusinessConfig proto, Updateset<BusinessConfig> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			BusinessConfigMapper mapper = session.getMapper(BusinessConfigMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for BusinessConfig.", e);
		}
	}

	public int updateBaseConfigByDomain(BusinessConfig proto, Updateset<BusinessConfig> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			BusinessConfigMapper mapper = session.getMapper(BusinessConfigMapper.class);
			int count = mapper.updateBaseConfigByDomain(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateBaseConfigByDomain for BusinessConfig.", e);
		}
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
