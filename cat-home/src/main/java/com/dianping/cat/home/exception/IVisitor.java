package com.dianping.cat.home.exception;

import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.home.exception.entity.ExceptionRuleConfig;

public interface IVisitor {

   void visitExceptionExclude(ExceptionExclude exceptionExclude);

   void visitExceptionLimit(ExceptionLimit exceptionLimit);

   void visitExceptionRuleConfig(ExceptionRuleConfig exceptionRuleConfig);
}
