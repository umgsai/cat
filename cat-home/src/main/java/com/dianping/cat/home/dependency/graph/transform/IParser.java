package com.dianping.cat.home.dependency.graph.transform;

import com.dianping.cat.home.dependency.graph.entity.TopologyEdge;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;

public interface IParser<T> {
   TopologyGraph parse(IMaker<T> maker, ILinker linker, T node);

   void parseForTopologyEdge(IMaker<T> maker, ILinker linker, TopologyEdge parent, T node);

   void parseForTopologyNode(IMaker<T> maker, ILinker linker, TopologyNode parent, T node);
}
