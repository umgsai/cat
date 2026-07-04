package com.dianping.cat.consumer.top.model.transform;

import com.dianping.cat.consumer.top.model.entity.Domain;
import com.dianping.cat.consumer.top.model.entity.Error;
import com.dianping.cat.consumer.top.model.entity.Machine;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;

public interface ILinker {

   boolean onDomain(TopReport parent, Domain domain);

   boolean onError(Segment parent, Error error);

   boolean onMachine(Segment parent, Machine machine);

   boolean onSegment(Domain parent, Segment segment);
}
