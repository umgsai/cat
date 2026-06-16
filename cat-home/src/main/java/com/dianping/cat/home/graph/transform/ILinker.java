package com.dianping.cat.home.graph.transform;

import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.entity.Item;
import com.dianping.cat.home.graph.entity.Segment;

public interface ILinker {

   public boolean onItem(Graph parent, Item item);

   public boolean onSegment(Item parent, Segment segment);
}
