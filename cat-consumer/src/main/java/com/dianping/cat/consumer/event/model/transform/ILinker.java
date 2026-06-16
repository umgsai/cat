package com.dianping.cat.consumer.event.model.transform;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.GraphTrend;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.consumer.event.model.entity.StatusCode;

public interface ILinker {

   public boolean onGraphTrend(EventType parent, GraphTrend graphTrend);

   public boolean onGraphTrend(EventName parent, GraphTrend graphTrend);

   public boolean onMachine(EventReport parent, Machine machine);

   public boolean onName(EventType parent, EventName name);

   public boolean onRange(EventName parent, Range range);

   public boolean onStatusCode(EventName parent, StatusCode statusCode);

   public boolean onType(Machine parent, EventType type);
}
