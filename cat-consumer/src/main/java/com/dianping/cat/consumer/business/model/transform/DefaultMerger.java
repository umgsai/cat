package com.dianping.cat.consumer.business.model.transform;

import java.util.Stack;

import com.dianping.cat.consumer.business.model.IEntity;
import com.dianping.cat.consumer.business.model.IVisitor;
import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;

public class DefaultMerger implements IVisitor {

   private Stack<Object> m_objs = new Stack<Object>();

   private BusinessReport m_businessReport;

   public DefaultMerger() {
   }

   public DefaultMerger(BusinessReport businessReport) {
      m_businessReport = businessReport;
      m_objs.push(businessReport);
   }

   public BusinessReport getBusinessReport() {
      return m_businessReport;
   }

   protected Stack<Object> getObjects() {
      return m_objs;
   }

   public <T> void merge(IEntity<T> to, IEntity<T> from) {
      m_objs.push(to);
      from.accept(this);
      m_objs.pop();
   }

   protected void mergeBusinessItem(BusinessItem to, BusinessItem from) {
      to.mergeAttributes(from);
   }

   protected void mergeBusinessReport(BusinessReport to, BusinessReport from) {
      to.mergeAttributes(from);
   }

   protected void mergeSegment(Segment to, Segment from) {
      to.mergeAttributes(from);
   }

   @Override
   public void visitBusinessItem(BusinessItem from) {
      BusinessItem to = (BusinessItem) m_objs.peek();

      mergeBusinessItem(to, from);
      visitBusinessItemChildren(to, from);
   }

   protected void visitBusinessItemChildren(BusinessItem to, BusinessItem from) {
      for (Segment source : from.getSegments().values()) {
         Segment target = to.findSegment(source.getId());

         if (target == null) {
            target = new Segment(source.getId());
            to.addSegment(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitBusinessReport(BusinessReport from) {
      BusinessReport to = (BusinessReport) m_objs.peek();

      mergeBusinessReport(to, from);
      visitBusinessReportChildren(to, from);
   }

   protected void visitBusinessReportChildren(BusinessReport to, BusinessReport from) {
      for (BusinessItem source : from.getBusinessItems().values()) {
         BusinessItem target = to.findBusinessItem(source.getId());

         if (target == null) {
            target = new BusinessItem(source.getId());
            to.addBusinessItem(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitSegment(Segment from) {
      Segment to = (Segment) m_objs.peek();

      mergeSegment(to, from);
      visitSegmentChildren(to, from);
   }

   protected void visitSegmentChildren(Segment to, Segment from) {
   }
}
