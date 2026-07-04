package com.dianping.cat.consumer.business.model.transform;

import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;

public interface IMaker<T> {

   BusinessItem buildBusinessItem(T node);

   BusinessReport buildBusinessReport(T node);

   Segment buildSegment(T node);
}
