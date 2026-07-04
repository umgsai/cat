package com.dianping.cat.home.graph;

import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.entity.Item;
import com.dianping.cat.home.graph.entity.Segment;

public interface IVisitor {

   void visitGraph(Graph graph);

   void visitItem(Item item);

   void visitSegment(Segment segment);
}
