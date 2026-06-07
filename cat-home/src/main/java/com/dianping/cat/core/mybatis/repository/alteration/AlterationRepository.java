package com.dianping.cat.core.mybatis.repository.alteration;

import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.alteration.dao.AlterationMapper;
import com.dianping.cat.core.mybatis.generated.alteration.dao.data.AlterationDO;
import com.dianping.cat.home.dal.report.Alteration;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class AlterationRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/AlterationMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return AlterationMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public Alteration createLocal() {
		return new Alteration();
	}

	public int deleteByPK(Alteration proto) throws DalException {
		try (SqlSession session = openSession()) {
			AlterationMapper mapper = session.getMapper(AlterationMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for Alteration.", e);
		}
	}

	public List<Alteration> findByTypeDruation(java.util.Date startTime, java.util.Date endTime, String type, Readset<Alteration> readset) throws DalException {
		try (SqlSession session = openSession()) {
			AlterationMapper mapper = session.getMapper(AlterationMapper.class);
			AlterationDO record = new AlterationDO();
			record.setStartTime(startTime);
			record.setEndTime(endTime);
			record.setType(type);
			return mapper.findByTypeDruation(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findByTypeDruation for Alteration.", e);
		}
	}

	public List<Alteration> findByDtdh(java.util.Date startTime, java.util.Date endTime, String type, String domain, String hostname, Readset<Alteration> readset) throws DalException {
		try (SqlSession session = openSession()) {
			AlterationMapper mapper = session.getMapper(AlterationMapper.class);
			AlterationDO record = new AlterationDO();
			record.setStartTime(startTime);
			record.setEndTime(endTime);
			record.setType(type);
			record.setDomain(domain);
			record.setHostname(hostname);
			return mapper.findByDtdh(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findByDtdh for Alteration.", e);
		}
	}

	public List<Alteration> findByDtdhTypes(java.util.Date startTime, java.util.Date endTime, String type, String domain, String hostname, String[] types, Readset<Alteration> readset) throws DalException {
		try (SqlSession session = openSession()) {
			AlterationMapper mapper = session.getMapper(AlterationMapper.class);
			AlterationDO record = new AlterationDO();
			record.setStartTime(startTime);
			record.setEndTime(endTime);
			record.setType(type);
			record.setDomain(domain);
			record.setHostname(hostname);
			record.setTypes(types);
			return mapper.findByDtdhTypes(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findByDtdhTypes for Alteration.", e);
		}
	}

	public List<Alteration> findByDomainAndTime(java.util.Date startTime, java.util.Date endTime, String domain, Readset<Alteration> readset) throws DalException {
		try (SqlSession session = openSession()) {
			AlterationMapper mapper = session.getMapper(AlterationMapper.class);
			AlterationDO record = new AlterationDO();
			record.setStartTime(startTime);
			record.setEndTime(endTime);
			record.setDomain(domain);
			return mapper.findByDomainAndTime(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findByDomainAndTime for Alteration.", e);
		}
	}

	public Alteration findByPK(int keyId, Readset<Alteration> readset) throws DalException {
		try (SqlSession session = openSession()) {
			AlterationMapper mapper = session.getMapper(AlterationMapper.class);
			AlterationDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for Alteration.", e);
		}
	}

	public int insert(Alteration proto) throws DalException {
		try (SqlSession session = openSession()) {
			AlterationMapper mapper = session.getMapper(AlterationMapper.class);
			AlterationDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for Alteration.", e);
		}
	}

	public int updateByPK(Alteration proto, Updateset<Alteration> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			AlterationMapper mapper = session.getMapper(AlterationMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for Alteration.", e);
		}
	}

	private Alteration requireFound(AlterationDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No Alteration found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private Alteration toModel(AlterationDO record) {
		Alteration model = new Alteration();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getType() != null) {
			model.setType(record.getType());
		}
		if (record.getTitle() != null) {
			model.setTitle(record.getTitle());
		}
		if (record.getDomain() != null) {
			model.setDomain(record.getDomain());
		}
		if (record.getHostname() != null) {
			model.setHostname(record.getHostname());
		}
		if (record.getIp() != null) {
			model.setIp(record.getIp());
		}
		if (record.getDate() != null) {
			model.setDate(record.getDate());
		}
		if (record.getUser() != null) {
			model.setUser(record.getUser());
		}
		if (record.getAltGroup() != null) {
			model.setAltGroup(record.getAltGroup());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getUrl() != null) {
			model.setUrl(record.getUrl());
		}
		if (record.getStatus() != null) {
			model.setStatus(record.getStatus());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		model.afterLoad();
		return model;
	}

	private AlterationDO toRecord(Alteration model) {
		AlterationDO record = new AlterationDO();

		record.setId(model.getId());
		record.setType(model.getType());
		record.setTitle(model.getTitle());
		record.setDomain(model.getDomain());
		record.setHostname(model.getHostname());
		record.setIp(model.getIp());
		record.setDate(model.getDate());
		record.setUser(model.getUser());
		record.setAltGroup(model.getAltGroup());
		record.setContent(model.getContent());
		record.setUrl(model.getUrl());
		record.setStatus(model.getStatus());
		record.setCreationDate(model.getCreationDate());
		record.setKeyId(model.getKeyId());
		record.setStartTime(model.getStartTime());
		record.setEndTime(model.getEndTime());
		record.setTypes(model.getTypes());
		return record;
	}
}
