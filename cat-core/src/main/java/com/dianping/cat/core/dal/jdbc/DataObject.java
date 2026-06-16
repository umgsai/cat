package com.dianping.cat.core.dal.jdbc;

import java.util.HashMap;
import java.util.Map;

public abstract class DataObject {
	private Map<String, Object> m_queryHints;

	private final Map<DataField, Boolean> m_usedFields = new HashMap<>();

	public void afterLoad() {
	}

	public void beforeSave() {
	}

	protected void clearUsage() {
		m_usedFields.clear();
	}

	public Map<String, Object> getQueryHints() {
		if (m_queryHints == null) {
			m_queryHints = new HashMap<>();
		}
		return m_queryHints;
	}

	public boolean isFieldUsed(DataField field) {
		return Boolean.TRUE.equals(m_usedFields.get(field));
	}

	protected void setFieldUsed(DataField field, boolean used) {
		m_usedFields.put(field, used);
	}

	public void setQueryHint(String name, Object value) {
		getQueryHints().put(name, value);
	}
}
