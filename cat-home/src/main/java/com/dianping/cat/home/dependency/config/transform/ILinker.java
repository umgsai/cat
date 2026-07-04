package com.dianping.cat.home.dependency.config.transform;

import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.config.entity.NodeConfig;
import com.dianping.cat.home.dependency.config.entity.TopologyGraphConfig;

public interface ILinker {

   boolean onDomainConfig(NodeConfig parent, DomainConfig domainConfig);

   boolean onEdgeConfig(TopologyGraphConfig parent, EdgeConfig edgeConfig);

   boolean onNodeConfig(TopologyGraphConfig parent, NodeConfig nodeConfig);
}
