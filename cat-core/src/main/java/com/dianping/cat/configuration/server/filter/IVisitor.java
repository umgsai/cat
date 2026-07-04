package com.dianping.cat.configuration.server.filter;

import com.dianping.cat.configuration.server.filter.entity.AtomicTreeConfig;
import com.dianping.cat.configuration.server.filter.entity.CrashLogDomain;
import com.dianping.cat.configuration.server.filter.entity.ServerFilterConfig;

public interface IVisitor {

   void visitAtomicTreeConfig(AtomicTreeConfig atomicTreeConfig);

   void visitCrashLogDomain(CrashLogDomain crashLogDomain);

   void visitServerFilterConfig(ServerFilterConfig serverFilterConfig);
}
