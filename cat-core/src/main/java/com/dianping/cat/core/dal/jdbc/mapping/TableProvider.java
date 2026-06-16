package com.dianping.cat.core.dal.jdbc.mapping;

import java.util.Map;

public interface TableProvider {
	String getDataSourceName(Map<String, Object> hints, String logicalTableName);

	String getPhysicalTableName(Map<String, Object> hints, String logicalTableName);
}
