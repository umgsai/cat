package com.dianping.cat.consumer.business.model.transform;

import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;

public interface ILinker {

   public boolean onBusinessItem(BusinessReport parent, BusinessItem businessItem);

   public boolean onSegment(BusinessItem parent, Segment segment);
}
