package com.dianping.cat.configuration.reload.entity;

import static com.dianping.cat.configuration.reload.Constants.ATTR_ID;
import static com.dianping.cat.configuration.reload.Constants.ENTITY_REPORT_TYPE;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.configuration.reload.BaseEntity;
import com.dianping.cat.configuration.reload.IVisitor;

public class ReportType extends BaseEntity<ReportType> {
   private String m_id;

   private List<ReportPeriod> m_reportPeriods = new ArrayList<ReportPeriod>();

   public ReportType() {
   }

   public ReportType(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitReportType(this);
   }

   public ReportType addReportPeriod(ReportPeriod reportPeriod) {
      m_reportPeriods.add(reportPeriod);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ReportType) {
         ReportType _o = (ReportType) obj;

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

   public List<ReportPeriod> getReportPeriods() {
      return m_reportPeriods;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(ReportType other) {
      assertAttributeEquals(other, ENTITY_REPORT_TYPE, ATTR_ID, m_id, other.getId());

   }

   public ReportType setId(String id) {
      m_id = id;
      return this;
   }

}
