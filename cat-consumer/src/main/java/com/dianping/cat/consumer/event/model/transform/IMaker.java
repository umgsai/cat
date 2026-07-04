package com.dianping.cat.consumer.event.model.transform;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.GraphTrend;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.consumer.event.model.entity.StatusCode;

public interface IMaker<T> {

   String buildDomain(T node);

   EventReport buildEventReport(T node);

   GraphTrend buildGraphTrend(T node);

   String buildIp(T node);

   Machine buildMachine(T node);

   EventName buildName(T node);

   Range buildRange(T node);

   StatusCode buildStatusCode(T node);

   EventType buildType(T node);
}
