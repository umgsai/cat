package com.dianping.cat.consumer.business.model;

import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;

public interface IVisitor {

   void visitBusinessItem(BusinessItem businessItem);

   void visitBusinessReport(BusinessReport businessReport);

   void visitSegment(Segment segment);
}
