package com.dianping.cat.core.mybatis.repository.hostinfo;

import com.dianping.cat.core.dal.Hostinfo;
import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.hostinfo.dao.HostinfoMapper;
import com.dianping.cat.core.mybatis.generated.hostinfo.dao.data.HostinfoDO;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class HostinfoRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/HostinfoMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return HostinfoMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public Hostinfo createLocal() {
		return new Hostinfo();
	}

	public int deleteByPK(Hostinfo proto) throws DalException {
		try (SqlSession session = openSession()) {
			HostinfoMapper mapper = session.getMapper(HostinfoMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for Hostinfo.", e);
		}
	}

	public List<Hostinfo> findAllIp(Readset<Hostinfo> readset) throws DalException {
		try (SqlSession session = openSession()) {
			HostinfoMapper mapper = session.getMapper(HostinfoMapper.class);
			HostinfoDO record = new HostinfoDO();
			return mapper.findAllIp(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findAllIp for Hostinfo.", e);
		}
	}

	public Hostinfo findByPK(int keyId, Readset<Hostinfo> readset) throws DalException {
		try (SqlSession session = openSession()) {
			HostinfoMapper mapper = session.getMapper(HostinfoMapper.class);
			HostinfoDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for Hostinfo.", e);
		}
	}

	public Hostinfo findByIp(String ip, Readset<Hostinfo> readset) throws DalException {
		try (SqlSession session = openSession()) {
			HostinfoMapper mapper = session.getMapper(HostinfoMapper.class);
			HostinfoDO record = new HostinfoDO();
			record.setIp(ip);
			HostinfoDO result = mapper.findByIp(record).stream().findFirst().orElse(null);
			return requireFound(result, "findByIp", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByIp for Hostinfo.", e);
		}
	}

	public int insert(Hostinfo proto) throws DalException {
		try (SqlSession session = openSession()) {
			HostinfoMapper mapper = session.getMapper(HostinfoMapper.class);
			HostinfoDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for Hostinfo.", e);
		}
	}

	public int updateByPK(Hostinfo proto, Updateset<Hostinfo> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			HostinfoMapper mapper = session.getMapper(HostinfoMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for Hostinfo.", e);
		}
	}

	private Hostinfo requireFound(HostinfoDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No Hostinfo found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private Hostinfo toModel(HostinfoDO record) {
		Hostinfo model = new Hostinfo();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getIp() != null) {
			model.setIp(record.getIp());
		}
		if (record.getDomain() != null) {
			model.setDomain(record.getDomain());
		}
		if (record.getHostname() != null) {
			model.setHostname(record.getHostname());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		if (record.getLastModifiedDate() != null) {
			model.setLastModifiedDate(record.getLastModifiedDate());
		}
		model.afterLoad();
		return model;
	}

	private HostinfoDO toRecord(Hostinfo model) {
		HostinfoDO record = new HostinfoDO();

		record.setId(model.getId());
		record.setIp(model.getIp());
		record.setDomain(model.getDomain());
		record.setHostname(model.getHostname());
		record.setCreationDate(model.getCreationDate());
		record.setLastModifiedDate(model.getLastModifiedDate());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
