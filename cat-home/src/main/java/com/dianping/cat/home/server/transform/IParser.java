package com.dianping.cat.home.server.transform;

import com.dianping.cat.home.server.entity.Group;
import com.dianping.cat.home.server.entity.Item;
import com.dianping.cat.home.server.entity.Segment;
import com.dianping.cat.home.server.entity.ServerMetricConfig;

public interface IParser<T> {
   ServerMetricConfig parse(IMaker<T> maker, ILinker linker, T node);

   void parseForGroup(IMaker<T> maker, ILinker linker, Group parent, T node);

   void parseForItem(IMaker<T> maker, ILinker linker, Item parent, T node);

   void parseForSegment(IMaker<T> maker, ILinker linker, Segment parent, T node);
}
