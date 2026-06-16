package com.dianping.cat.core.dal.jdbc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Readset<T> {
	private final List<Readset<T>> m_children;

	private final List<DataField> m_fields;

	@SafeVarargs
	public Readset(Readset<T>... children) {
		m_children = Collections.unmodifiableList(Arrays.asList(children));
		m_fields = Collections.emptyList();
	}

	public Readset(DataField... fields) {
		m_children = Collections.emptyList();
		m_fields = Collections.unmodifiableList(Arrays.asList(fields));
	}

	public List<Readset<T>> getChildren() {
		return m_children;
	}

	public List<DataField> getFields() {
		return m_fields;
	}
}
