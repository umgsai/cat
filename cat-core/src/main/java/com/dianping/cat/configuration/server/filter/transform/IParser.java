package com.dianping.cat.configuration.server.filter.transform;

import com.dianping.cat.configuration.server.filter.entity.AtomicTreeConfig;
import com.dianping.cat.configuration.server.filter.entity.CrashLogDomain;
import com.dianping.cat.configuration.server.filter.entity.ServerFilterConfig;

public interface IParser<T> {
   public ServerFilterConfig parse(IMaker<T> maker, ILinker linker, T node);

   public void parseForAtomicTreeConfig(IMaker<T> maker, ILinker linker, AtomicTreeConfig parent, T node);

   public void parseForCrashLogDomain(IMaker<T> maker, ILinker linker, CrashLogDomain parent, T node);
}
