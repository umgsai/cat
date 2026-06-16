package com.dianping.cat.consumer.business.model.transform;

import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;

public interface IMaker<T> {

   public BusinessItem buildBusinessItem(T node);

   public BusinessReport buildBusinessReport(T node);

   public Segment buildSegment(T node);
}
