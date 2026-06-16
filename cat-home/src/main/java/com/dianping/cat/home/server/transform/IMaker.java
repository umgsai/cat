package com.dianping.cat.home.server.transform;

import com.dianping.cat.home.server.entity.Group;
import com.dianping.cat.home.server.entity.Item;
import com.dianping.cat.home.server.entity.Segment;
import com.dianping.cat.home.server.entity.ServerMetricConfig;

public interface IMaker<T> {

   public Group buildGroup(T node);

   public Item buildItem(T node);

   public Segment buildSegment(T node);

   public ServerMetricConfig buildServerMetricConfig(T node);
}
