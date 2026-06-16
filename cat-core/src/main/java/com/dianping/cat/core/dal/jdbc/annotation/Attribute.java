package com.dianping.cat.core.dal.jdbc.annotation;

public @interface Attribute {
	boolean autoIncrement() default false;

	String field();

	String insertExpr() default "";

	boolean nullable() default true;

	boolean primaryKey() default false;

	String selectExpr() default "";

	String updateExpr() default "";
}
