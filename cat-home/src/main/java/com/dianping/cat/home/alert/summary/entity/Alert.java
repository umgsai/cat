package com.dianping.cat.home.alert.summary.entity;

import com.dianping.cat.home.alert.summary.BaseEntity;
import com.dianping.cat.home.alert.summary.IVisitor;

public class Alert extends BaseEntity<Alert> {
   private java.util.Date m_alertTime;

   private String m_type;

   private String m_metric;

   private String m_context;

   private String m_domain;

   private Integer m_count;

   public Alert() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitAlert(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Alert) {
         Alert _o = (Alert) obj;

         if (!equals(getAlertTime(), _o.getAlertTime())) {
            return false;
         }

         if (!equals(getType(), _o.getType())) {
            return false;
         }

         if (!equals(getMetric(), _o.getMetric())) {
            return false;
         }

         if (!equals(getContext(), _o.getContext())) {
            return false;
         }

         if (!equals(getDomain(), _o.getDomain())) {
            return false;
         }

         if (!equals(getCount(), _o.getCount())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public java.util.Date getAlertTime() {
      return m_alertTime;
   }

   public String getContext() {
      return m_context;
   }

   public Integer getCount() {
      return m_count;
   }

   public String getDomain() {
      return m_domain;
   }

   public String getMetric() {
      return m_metric;
   }

   public String getType() {
      return m_type;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_alertTime == null ? 0 : m_alertTime.hashCode());
      hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());
      hash = hash * 31 + (m_metric == null ? 0 : m_metric.hashCode());
      hash = hash * 31 + (m_context == null ? 0 : m_context.hashCode());
      hash = hash * 31 + (m_domain == null ? 0 : m_domain.hashCode());
      hash = hash * 31 + (m_count == null ? 0 : m_count.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(Alert other) {
      if (other.getAlertTime() != null) {
         m_alertTime = other.getAlertTime();
      }

      if (other.getType() != null) {
         m_type = other.getType();
      }

      if (other.getMetric() != null) {
         m_metric = other.getMetric();
      }

      if (other.getContext() != null) {
         m_context = other.getContext();
      }

      if (other.getDomain() != null) {
         m_domain = other.getDomain();
      }

      if (other.getCount() != null) {
         m_count = other.getCount();
      }
   }

   public Alert setAlertTime(java.util.Date alertTime) {
      m_alertTime = alertTime;
      return this;
   }

   public Alert setContext(String context) {
      m_context = context;
      return this;
   }

   public Alert setCount(Integer count) {
      m_count = count;
      return this;
   }

   public Alert setDomain(String domain) {
      m_domain = domain;
      return this;
   }

   public Alert setMetric(String metric) {
      m_metric = metric;
      return this;
   }

   public Alert setType(String type) {
      m_type = type;
      return this;
   }

}
