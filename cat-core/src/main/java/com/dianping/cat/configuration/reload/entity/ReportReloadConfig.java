package com.dianping.cat.configuration.reload.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.configuration.reload.BaseEntity;
import com.dianping.cat.configuration.reload.IVisitor;

public class ReportReloadConfig extends BaseEntity<ReportReloadConfig> {
   private Map<String, ReportType> m_reportTypes = new LinkedHashMap<String, ReportType>();

   public ReportReloadConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitReportReloadConfig(this);
   }

   public ReportReloadConfig addReportType(ReportType reportType) {
      m_reportTypes.put(reportType.getId(), reportType);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ReportReloadConfig) {
         ReportReloadConfig _o = (ReportReloadConfig) obj;

         if (!equals(getReportTypes(), _o.getReportTypes())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public ReportType findReportType(String id) {
      return m_reportTypes.get(id);
   }

   public Map<String, ReportType> getReportTypes() {
      return m_reportTypes;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_reportTypes == null ? 0 : m_reportTypes.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(ReportReloadConfig other) {
   }

   public ReportType removeReportType(String id) {
      return m_reportTypes.remove(id);
   }

}
