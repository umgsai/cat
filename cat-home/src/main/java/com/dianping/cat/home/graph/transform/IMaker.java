package com.dianping.cat.home.graph.transform;

import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.entity.Item;
import com.dianping.cat.home.graph.entity.Segment;

public interface IMaker<T> {

   public Graph buildGraph(T node);

   public Item buildItem(T node);

   public Segment buildSegment(T node);
}
