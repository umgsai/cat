package com.dianping.cat.consumer.top.model.transform;

import com.dianping.cat.consumer.top.model.entity.Domain;
import com.dianping.cat.consumer.top.model.entity.Error;
import com.dianping.cat.consumer.top.model.entity.Machine;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;

public interface IMaker<T> {

   Domain buildDomain(T node);

   Error buildError(T node);

   Machine buildMachine(T node);

   Segment buildSegment(T node);

   TopReport buildTopReport(T node);
}
