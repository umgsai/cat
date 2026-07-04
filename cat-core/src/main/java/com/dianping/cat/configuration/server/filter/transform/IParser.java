package com.dianping.cat.configuration.server.filter.transform;

import com.dianping.cat.configuration.server.filter.entity.AtomicTreeConfig;
import com.dianping.cat.configuration.server.filter.entity.CrashLogDomain;
import com.dianping.cat.configuration.server.filter.entity.ServerFilterConfig;

public interface IParser<T> {
   ServerFilterConfig parse(IMaker<T> maker, ILinker linker, T node);

   void parseForAtomicTreeConfig(IMaker<T> maker, ILinker linker, AtomicTreeConfig parent, T node);

   void parseForCrashLogDomain(IMaker<T> maker, ILinker linker, CrashLogDomain parent, T node);
}
