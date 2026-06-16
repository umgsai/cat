package com.dianping.cat.home.graph;

import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.entity.Item;
import com.dianping.cat.home.graph.entity.Segment;

public interface IVisitor {

   public void visitGraph(Graph graph);

   public void visitItem(Item item);

   public void visitSegment(Segment segment);
}
