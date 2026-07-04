package com.dianping.cat.home.dependency.config.transform;

import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.config.entity.NodeConfig;
import com.dianping.cat.home.dependency.config.entity.TopologyGraphConfig;

public interface IParser<T> {
   TopologyGraphConfig parse(IMaker<T> maker, ILinker linker, T node);

   void parseForDomainConfig(IMaker<T> maker, ILinker linker, DomainConfig parent, T node);

   void parseForEdgeConfig(IMaker<T> maker, ILinker linker, EdgeConfig parent, T node);

   void parseForNodeConfig(IMaker<T> maker, ILinker linker, NodeConfig parent, T node);
}
