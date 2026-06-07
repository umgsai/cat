package com.dianping.cat.core.mybatis.repository.alteration;

import com.dianping.cat.core.mybatis.repository.SpringBackedRepositorySupport;
import com.dianping.cat.core.mybatis.generated.alteration.dao.AlterationMapper;
import com.dianping.cat.core.mybatis.generated.alteration.dao.data.AlterationDO;
import com.dianping.cat.home.dal.report.Alteration;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class AlterationRepository extends SpringBackedRepositorySupport<AlterationMapper> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlterationRepository.class);

	private static final String MAPPER_RESOURCE = "mybatis/mapper/AlterationMapper.xml";

	public AlterationRepository() {
		super(AlterationMapper.class, MAPPER_RESOURCE,
				"AlterationRepository is using Spring managed AlterationMapper.");
	}

	public Alteration createLocal() {
		return new Alteration();
	}

	public int deleteByPK(Alteration proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper(LOGGER).deleteByPrimaryKey(proto.getKeyId()));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(AlterationMapper.class).deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for Alteration.", e);
		}
	}

	public List<Alteration> findByTypeDruation(java.util.Date startTime, java.util.Date endTime, String type, Readset<Alteration> readset) throws DalException {
		AlterationMapper mapper = springMapper(LOGGER);
		AlterationDO record = new AlterationDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setType(type);
		if (mapper != null) {
			return mapper.findByTypeDruation(record).stream().map(this::toModel).collect(Collectors.toList());
		}

		try (SqlSession session = openSession()) {
			return session.getMapper(AlterationMapper.class).findByTypeDruation(record).stream()
					.map(this::toModel)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findByTypeDruation for Alteration.", e);
		}
	}

	public List<Alteration> findByDtdh(java.util.Date startTime, java.util.Date endTime, String type, String domain, String hostname, Readset<Alteration> readset) throws DalException {
		AlterationMapper mapper = springMapper(LOGGER);
		AlterationDO record = new AlterationDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setType(type);
		record.setDomain(domain);
		record.setHostname(hostname);
		if (mapper != null) {
			return mapper.findByDtdh(record).stream().map(this::toModel).collect(Collectors.toList());
		}

		try (SqlSession session = openSession()) {
			return session.getMapper(AlterationMapper.class).findByDtdh(record).stream()
					.map(this::toModel)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findByDtdh for Alteration.", e);
		}
	}

	public List<Alteration> findByDtdhTypes(java.util.Date startTime, java.util.Date endTime, String type, String domain, String hostname, String[] types, Readset<Alteration> readset) throws DalException {
		AlterationMapper mapper = springMapper(LOGGER);
		AlterationDO record = new AlterationDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setType(type);
		record.setDomain(domain);
		record.setHostname(hostname);
		record.setTypes(types);
		if (mapper != null) {
			return mapper.findByDtdhTypes(record).stream().map(this::toModel).collect(Collectors.toList());
		}

		try (SqlSession session = openSession()) {
			return session.getMapper(AlterationMapper.class).findByDtdhTypes(record).stream()
					.map(this::toModel)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findByDtdhTypes for Alteration.", e);
		}
	}

	public List<Alteration> findByDomainAndTime(java.util.Date startTime, java.util.Date endTime, String domain, Readset<Alteration> readset) throws DalException {
		AlterationMapper mapper = springMapper(LOGGER);
		AlterationDO record = new AlterationDO();

		record.setStartTime(startTime);
		record.setEndTime(endTime);
		record.setDomain(domain);
		if (mapper != null) {
			return mapper.findByDomainAndTime(record).stream().map(this::toModel).collect(Collectors.toList());
		}

		try (SqlSession session = openSession()) {
			return session.getMapper(AlterationMapper.class).findByDomainAndTime(record).stream()
					.map(this::toModel)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findByDomainAndTime for Alteration.", e);
		}
	}

	public Alteration findByPK(int keyId, Readset<Alteration> readset) throws DalException {
		AlterationMapper mapper = springMapper(LOGGER);

		if (mapper != null) {
			return requireFound(mapper.findByPrimaryKey(keyId), "primary key", String.valueOf(keyId));
		}

		try (SqlSession session = openSession()) {
			AlterationDO record = session.getMapper(AlterationMapper.class).findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for Alteration.", e);
		}
	}

	public int insert(Alteration proto) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			AlterationDO record = toRecord(proto);
			int count = transactionTemplate.execute(status -> springMapper(LOGGER).insert(record));

			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		}

		try (SqlSession session = openSession()) {
			AlterationDO record = toRecord(proto);
			int count = session.getMapper(AlterationMapper.class).insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for Alteration.", e);
		}
	}

	public int updateByPK(Alteration proto, Updateset<Alteration> updateset) throws DalException {
		TransactionTemplate transactionTemplate = springTransactionTemplate();

		if (transactionTemplate != null) {
			return transactionTemplate.execute(status -> springMapper(LOGGER).updateByPrimaryKey(toRecord(proto)));
		}

		try (SqlSession session = openSession()) {
			int count = session.getMapper(AlterationMapper.class).updateByPrimaryKey(toRecord(proto));
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
