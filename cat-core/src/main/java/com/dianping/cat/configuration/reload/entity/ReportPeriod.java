package com.dianping.cat.configuration.reload.entity;

import com.dianping.cat.configuration.reload.BaseEntity;
import com.dianping.cat.configuration.reload.IVisitor;

public class ReportPeriod extends BaseEntity<ReportPeriod> {
   private String m_id;

   public ReportPeriod() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitReportPeriod(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ReportPeriod) {
         ReportPeriod _o = (ReportPeriod) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public String getId() {
      return m_id;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(ReportPeriod other) {
      if (other.getId() != null) {
         m_id = other.getId();
      }
   }

   public ReportPeriod setId(String id) {
      m_id = id;
      return this;
   }

}
