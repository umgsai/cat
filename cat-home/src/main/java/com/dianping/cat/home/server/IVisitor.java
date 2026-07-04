package com.dianping.cat.home.server;

import com.dianping.cat.home.server.entity.Group;
import com.dianping.cat.home.server.entity.Item;
import com.dianping.cat.home.server.entity.Segment;
import com.dianping.cat.home.server.entity.ServerMetricConfig;

public interface IVisitor {

   void visitGroup(Group group);

   void visitItem(Item item);

   void visitSegment(Segment segment);

   void visitServerMetricConfig(ServerMetricConfig serverMetricConfig);
}
