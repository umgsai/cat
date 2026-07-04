package com.dianping.cat.consumer.event.model.transform;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.GraphTrend;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.consumer.event.model.entity.StatusCode;

public interface IParser<T> {
   EventReport parse(IMaker<T> maker, ILinker linker, T node);

   void parseForGraphTrend(IMaker<T> maker, ILinker linker, GraphTrend parent, T node);

   void parseForMachine(IMaker<T> maker, ILinker linker, Machine parent, T node);

   void parseForEventName(IMaker<T> maker, ILinker linker, EventName parent, T node);

   void parseForRange(IMaker<T> maker, ILinker linker, Range parent, T node);

   void parseForStatusCode(IMaker<T> maker, ILinker linker, StatusCode parent, T node);

   void parseForEventType(IMaker<T> maker, ILinker linker, EventType parent, T node);
}
