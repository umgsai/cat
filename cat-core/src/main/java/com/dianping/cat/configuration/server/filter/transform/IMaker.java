package com.dianping.cat.configuration.server.filter.transform;

import com.dianping.cat.configuration.server.filter.entity.AtomicTreeConfig;
import com.dianping.cat.configuration.server.filter.entity.CrashLogDomain;
import com.dianping.cat.configuration.server.filter.entity.ServerFilterConfig;

public interface IMaker<T> {

   AtomicTreeConfig buildAtomicTreeConfig(T node);

   CrashLogDomain buildCrashLogDomain(T node);

   String buildDomain(T node);

   ServerFilterConfig buildServerFilterConfig(T node);

   String buildTransactionName(T node);

   String buildTransactionType(T node);
}
