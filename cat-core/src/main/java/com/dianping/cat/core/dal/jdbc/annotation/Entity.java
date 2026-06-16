package com.dianping.cat.core.dal.jdbc.annotation;

public @interface Entity {
	String alias();

	String logicalName();

	String physicalName();
}
