package com.dianping.cat.home.server.transform;

import com.dianping.cat.home.server.entity.Group;
import com.dianping.cat.home.server.entity.Item;
import com.dianping.cat.home.server.entity.Segment;
import com.dianping.cat.home.server.entity.ServerMetricConfig;

public interface ILinker {

   public boolean onGroup(ServerMetricConfig parent, Group group);

   public boolean onItem(Group parent, Item item);

   public boolean onSegment(Item parent, Segment segment);
}
