package com.dianping.cat.home.exception.transform;

import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.home.exception.entity.ExceptionRuleConfig;

public interface IMaker<T> {

   ExceptionExclude buildExceptionExclude(T node);

   ExceptionLimit buildExceptionLimit(T node);

   ExceptionRuleConfig buildExceptionRuleConfig(T node);
}
