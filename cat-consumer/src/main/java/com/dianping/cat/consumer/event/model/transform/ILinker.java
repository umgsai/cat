package com.dianping.cat.consumer.event.model.transform;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.GraphTrend;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.consumer.event.model.entity.StatusCode;

public interface ILinker {

   boolean onGraphTrend(EventType parent, GraphTrend graphTrend);

   boolean onGraphTrend(EventName parent, GraphTrend graphTrend);

   boolean onMachine(EventReport parent, Machine machine);

   boolean onName(EventType parent, EventName name);

   boolean onRange(EventName parent, Range range);

   boolean onStatusCode(EventName parent, StatusCode statusCode);

   boolean onType(Machine parent, EventType type);
}
