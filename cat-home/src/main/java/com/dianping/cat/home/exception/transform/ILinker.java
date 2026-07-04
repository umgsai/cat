package com.dianping.cat.home.exception.transform;

import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.home.exception.entity.ExceptionRuleConfig;

public interface ILinker {

   boolean onExceptionExclude(ExceptionRuleConfig parent, ExceptionExclude exceptionExclude);

   boolean onExceptionLimit(ExceptionRuleConfig parent, ExceptionLimit exceptionLimit);
}
