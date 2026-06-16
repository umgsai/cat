package com.dianping.cat.core.dal.jdbc;

public class QueryDef {
	private final Class<?> m_entityClass;

	private final String m_name;

	private final boolean m_raw;

	private final String m_statement;

	private final QueryType m_type;

	public QueryDef(String name, Class<?> entityClass, QueryType type, String statement) {
		this(name, entityClass, type, statement, false);
	}

	public QueryDef(String name, Class<?> entityClass, QueryType type, String statement, boolean raw) {
		m_name = name;
		m_entityClass = entityClass;
		m_type = type;
		m_statement = statement;
		m_raw = raw;
	}

	public Class<?> getEntityClass() {
		return m_entityClass;
	}

	public String getName() {
		return m_name;
	}

	public QueryType getType() {
		return m_type;
	}

	public boolean isRaw() {
		return m_raw;
	}

	public boolean isStoreProcedure() {
		return false;
	}

	@Override
	public String toString() {
		return m_statement;
	}
}
