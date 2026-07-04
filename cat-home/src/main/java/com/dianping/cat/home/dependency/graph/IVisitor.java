package com.dianping.cat.home.dependency.graph;

import com.dianping.cat.home.dependency.graph.entity.TopologyEdge;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;

public interface IVisitor {

   void visitTopologyEdge(TopologyEdge topologyEdge);

   void visitTopologyGraph(TopologyGraph topologyGraph);

   void visitTopologyNode(TopologyNode topologyNode);
}
