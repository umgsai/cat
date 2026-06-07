package com.dianping.cat.core.mybatis.repository.user.define.rule;

import com.dianping.cat.alarm.UserDefineRule;
import com.dianping.cat.core.mybatis.MyBatisRepositorySupport;
import com.dianping.cat.core.mybatis.generated.user.define.rule.dao.UserDefineRuleMapper;
import com.dianping.cat.core.mybatis.generated.user.define.rule.dao.data.UserDefineRuleDO;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.session.SqlSession;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.dal.jdbc.Updateset;

public class UserDefineRuleRepository extends MyBatisRepositorySupport {
	private static final String MAPPER_RESOURCE = "mybatis/mapper/UserDefineRuleMapper.xml";

	@Override
	protected Class<?> getMapperClass() {
		return UserDefineRuleMapper.class;
	}

	@Override
	protected String getMapperResource() {
		return MAPPER_RESOURCE;
	}

	public UserDefineRule createLocal() {
		return new UserDefineRule();
	}

	public int deleteByPK(UserDefineRule proto) throws DalException {
		try (SqlSession session = openSession()) {
			UserDefineRuleMapper mapper = session.getMapper(UserDefineRuleMapper.class);
			int count = mapper.deleteByPrimaryKey(proto.getKeyId());
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing deleteByPK for UserDefineRule.", e);
		}
	}

	public UserDefineRule findByPK(int keyId, Readset<UserDefineRule> readset) throws DalException {
		try (SqlSession session = openSession()) {
			UserDefineRuleMapper mapper = session.getMapper(UserDefineRuleMapper.class);
			UserDefineRuleDO record = mapper.findByPrimaryKey(keyId);
			return requireFound(record, "primary key", String.valueOf(keyId));
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findByPK for UserDefineRule.", e);
		}
	}

	public UserDefineRule findMaxId(Readset<UserDefineRule> readset) throws DalException {
		try (SqlSession session = openSession()) {
			UserDefineRuleMapper mapper = session.getMapper(UserDefineRuleMapper.class);
			UserDefineRuleDO record = new UserDefineRuleDO();
			UserDefineRuleDO result = mapper.findMaxId(record).stream().findFirst().orElse(null);
			return requireFound(result, "findMaxId", record.toString());
		} catch (DalNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new DalException("Error when executing findMaxId for UserDefineRule.", e);
		}
	}

	public int insert(UserDefineRule proto) throws DalException {
		try (SqlSession session = openSession()) {
			UserDefineRuleMapper mapper = session.getMapper(UserDefineRuleMapper.class);
			UserDefineRuleDO record = toRecord(proto);
			int count = mapper.insert(record);
			session.commit();
			proto.setId(record.getId());
			proto.setKeyId(record.getId());
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing insert for UserDefineRule.", e);
		}
	}

	public int updateByPK(UserDefineRule proto, Updateset<UserDefineRule> updateset) throws DalException {
		try (SqlSession session = openSession()) {
			UserDefineRuleMapper mapper = session.getMapper(UserDefineRuleMapper.class);
			int count = mapper.updateByPrimaryKey(toRecord(proto));
			session.commit();
			return count;
		} catch (Exception e) {
			throw new DalException("Error when executing updateByPK for UserDefineRule.", e);
		}
	}

	private UserDefineRule requireFound(UserDefineRuleDO record, String field, String value) throws DalNotFoundException {
		if (record == null) {
			throw new DalNotFoundException("No UserDefineRule found by " + field + "(" + value + ").");
		}

		return toModel(record);
	}

	private UserDefineRule toModel(UserDefineRuleDO record) {
		UserDefineRule model = new UserDefineRule();

		if (record.getId() != null) {
			model.setId(record.getId());
		}
		if (record.getContent() != null) {
			model.setContent(record.getContent());
		}
		if (record.getCreationDate() != null) {
			model.setCreationDate(record.getCreationDate());
		}
		if (record.getMaxId() != null) {
			model.setMaxId(record.getMaxId());
		}
		model.afterLoad();
		return model;
	}

	private UserDefineRuleDO toRecord(UserDefineRule model) {
		UserDefineRuleDO record = new UserDefineRuleDO();

		record.setId(model.getId());
		record.setContent(model.getContent());
		record.setCreationDate(model.getCreationDate());
		record.setKeyId(model.getKeyId());
		return record;
	}
}
