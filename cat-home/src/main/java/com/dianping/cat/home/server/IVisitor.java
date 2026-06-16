package com.dianping.cat.home.server;

import com.dianping.cat.home.server.entity.Group;
import com.dianping.cat.home.server.entity.Item;
import com.dianping.cat.home.server.entity.Segment;
import com.dianping.cat.home.server.entity.ServerMetricConfig;

public interface IVisitor {

   public void visitGroup(Group group);

   public void visitItem(Item item);

   public void visitSegment(Segment segment);

   public void visitServerMetricConfig(ServerMetricConfig serverMetricConfig);
}
