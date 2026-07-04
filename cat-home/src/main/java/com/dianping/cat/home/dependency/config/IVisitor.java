package com.dianping.cat.home.dependency.config;

import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.config.entity.NodeConfig;
import com.dianping.cat.home.dependency.config.entity.TopologyGraphConfig;

public interface IVisitor {

   void visitDomainConfig(DomainConfig domainConfig);

   void visitEdgeConfig(EdgeConfig edgeConfig);

   void visitNodeConfig(NodeConfig nodeConfig);

   void visitTopologyGraphConfig(TopologyGraphConfig topologyGraphConfig);
}
