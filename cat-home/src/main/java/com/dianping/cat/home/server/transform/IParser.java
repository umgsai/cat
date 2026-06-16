package com.dianping.cat.home.server.transform;

import com.dianping.cat.home.server.entity.Group;
import com.dianping.cat.home.server.entity.Item;
import com.dianping.cat.home.server.entity.Segment;
import com.dianping.cat.home.server.entity.ServerMetricConfig;

public interface IParser<T> {
   public ServerMetricConfig parse(IMaker<T> maker, ILinker linker, T node);

   public void parseForGroup(IMaker<T> maker, ILinker linker, Group parent, T node);

   public void parseForItem(IMaker<T> maker, ILinker linker, Item parent, T node);

   public void parseForSegment(IMaker<T> maker, ILinker linker, Segment parent, T node);
}
