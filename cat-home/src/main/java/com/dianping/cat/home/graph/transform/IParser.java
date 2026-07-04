package com.dianping.cat.home.graph.transform;

import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.entity.Item;
import com.dianping.cat.home.graph.entity.Segment;

public interface IParser<T> {
   Graph parse(IMaker<T> maker, ILinker linker, T node);

   void parseForItem(IMaker<T> maker, ILinker linker, Item parent, T node);

   void parseForSegment(IMaker<T> maker, ILinker linker, Segment parent, T node);
}
