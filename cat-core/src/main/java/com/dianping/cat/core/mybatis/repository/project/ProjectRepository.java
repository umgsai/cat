package com.dianping.cat.core.mybatis.repository.project;

import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.project.dao.ProjectMapper;
import com.dianping.cat.core.mybatis.generated.project.dao.data.ProjectDO;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class ProjectRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/ProjectMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return ProjectMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public Project createLocal() {
		return new Project();
	}

	public int deleteByPK(Project proto) throws DalException {
		try (SqlSession session = openSession()) {
			ProjectMapper mapper = session.getMapper(ProjectMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for Project.", e);
		}
	}

	public List<Project> findAll(Readset<Project> readset) throws DalException {
		try (SqlSession session = openSession()) {
			ProjectMapper mapper = session.getMapper(ProjectMapper.class);
			ProjectDO record = new ProjectDO();
			return mapper.findAll(record).stream().map(this::toModel).collect(Collectors.toList());
		} catch (Exception e) {
			throw new DalException("Error when executing findAll for Project.", e);
		}
	}

	public Project findByPK(int keyId, Readset<Project> readset) throws DalException {
		try (SqlSession session = openSession()) {
			ProjectMapper mapper = session.getMapper(ProjectMapper.class);
			ProjectDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for Project.", e);
		}
	}

	public Project findByDomain(String domain, Readset<Project> readset) throws DalException {
		try (SqlSession session = openSession()) {
			ProjectMapper mapper = session.getMapper(ProjectMapper.class);
			ProjectDO record = new ProjectDO();
			record.setDomain(domain);
			ProjectDO result = mapper.findByDomain(record).stream().findFirst().orElse(null);
			return requireFound(result, "findByDomain", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByDomain for Project.", e);
		}
	}

	public Project findByCmdbDomain(String domain, Readset<Project> readset) throws DalException {
		try (SqlSession session = openSession()) {
			ProjectMapper mapper = session.getMapper(ProjectMapper.class);
			ProjectDO record = new ProjectDO();
			record.setDomain(domain);
			ProjectDO result = mapper.findByCmdbDomain(record).stream().findFirst().orElse(null);
			return requireFound(result, "findByCmdbDomain", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByCmdbDomain for Project.", e);
		}
	}

	public int insert(Project proto) throws DalException {
		try (SqlSession session = openSession()) {
			ProjectMapper mapper = session.getMapper(ProjectMapper.class);
			ProjectDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for Project.", e);
		}
	}

	public int updateByPK(Project proto, Updateset<Project> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			ProjectMapper mapper = session.getMapper(ProjectMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for Project.", e);
		}
	}

	private Project requireFound(ProjectDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No Project found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private Project toModel(ProjectDO record) {
		Project model = new Project();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getDomain() != null) {
			model.setDomain(record.getDomain());
		}
		if (record.getCmdbDomain() != null) {
			model.setCmdbDomain(record.getCmdbDomain());
		}
		if (record.getLevel() != null) {
			model.setLevel(record.getLevel());
		}
		if (record.getBu() != null) {
			model.setBu(record.getBu());
		}
		if (record.getCmdbProductline() != null) {
			model.setCmdbProductline(record.getCmdbProductline());
		}
		if (record.getOwner() != null) {
			model.setOwner(record.getOwner());
		}
		if (record.getEmail() != null) {
			model.setEmail(record.getEmail());
		}
		if (record.getPhone() != null) {
			model.setPhone(record.getPhone());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		if (record.getModifyDate() != null) {
			model.setModifyDate(record.getModifyDate());
		}
		model.afterLoad();
		return model;
	}

	private ProjectDO toRecord(Project model) {
		ProjectDO record = new ProjectDO();

		record.setId(model.getId());
		record.setDomain(model.getDomain());
		record.setCmdbDomain(model.getCmdbDomain());
		record.setLevel(model.getLevel());
		record.setBu(model.getBu());
		record.setCmdbProductline(model.getCmdbProductline());
		record.setOwner(model.getOwner());
		record.setEmail(model.getEmail());
		record.setPhone(model.getPhone());
		record.setCreationDate(model.getCreationDate());
		record.setModifyDate(model.getModifyDate());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
