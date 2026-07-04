package com.dianping.cat.home.graph.transform;

import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.entity.Item;
import com.dianping.cat.home.graph.entity.Segment;

public interface IMaker<T> {

   Graph buildGraph(T node);

   Item buildItem(T node);

   Segment buildSegment(T node);
}
