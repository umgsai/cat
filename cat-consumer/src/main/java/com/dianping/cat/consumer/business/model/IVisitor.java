package com.dianping.cat.consumer.business.model;

import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;

public interface IVisitor {

   public void visitBusinessItem(BusinessItem businessItem);

   public void visitBusinessReport(BusinessReport businessReport);

   public void visitSegment(Segment segment);
}
