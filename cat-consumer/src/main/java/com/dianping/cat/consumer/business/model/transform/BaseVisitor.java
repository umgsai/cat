package com.dianping.cat.consumer.business.model.transform;

import com.dianping.cat.consumer.business.model.IVisitor;
import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;

public abstract class BaseVisitor implements IVisitor {
   @Override
   public void visitBusinessItem(BusinessItem businessItem) {
      for (Segment segment : businessItem.getSegments().values()) {
         visitSegment(segment);
      }
   }

   @Override
   public void visitBusinessReport(BusinessReport businessReport) {
      for (BusinessItem businessItem : businessReport.getBusinessItems().values()) {
         visitBusinessItem(businessItem);
      }
   }

   @Override
   public void visitSegment(Segment segment) {
   }
}
