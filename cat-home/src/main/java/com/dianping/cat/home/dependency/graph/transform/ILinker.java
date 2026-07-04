package com.dianping.cat.home.dependency.graph.transform;

import com.dianping.cat.home.dependency.graph.entity.TopologyEdge;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;

public interface ILinker {

   boolean onTopologyEdge(TopologyGraph parent, TopologyEdge topologyEdge);

   boolean onTopologyNode(TopologyGraph parent, TopologyNode topologyNode);
}
