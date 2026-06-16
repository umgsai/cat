package com.dianping.cat.core.dal.jdbc.annotation;

public @interface Variable {
	int scale() default 0;

	int sqlType() default 0;
}
