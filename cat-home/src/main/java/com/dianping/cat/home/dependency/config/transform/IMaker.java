package com.dianping.cat.home.dependency.config.transform;

import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.config.entity.NodeConfig;
import com.dianping.cat.home.dependency.config.entity.TopologyGraphConfig;

public interface IMaker<T> {

   DomainConfig buildDomainConfig(T node);

   EdgeConfig buildEdgeConfig(T node);

   NodeConfig buildNodeConfig(T node);

   TopologyGraphConfig buildTopologyGraphConfig(T node);
}
