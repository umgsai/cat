package com.dianping.cat.home.graph.transform;

import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.entity.Item;
import com.dianping.cat.home.graph.entity.Segment;

public interface IParser<T> {
   public Graph parse(IMaker<T> maker, ILinker linker, T node);

   public void parseForItem(IMaker<T> maker, ILinker linker, Item parent, T node);

   public void parseForSegment(IMaker<T> maker, ILinker linker, Segment parent, T node);
}
