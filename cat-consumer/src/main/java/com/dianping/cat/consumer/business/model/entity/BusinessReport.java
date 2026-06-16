package com.dianping.cat.consumer.business.model.entity;

import static com.dianping.cat.consumer.business.model.Constants.ATTR_DOMAIN;
import static com.dianping.cat.consumer.business.model.Constants.ENTITY_BUSINESS_REPORT;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.consumer.business.model.BaseEntity;
import com.dianping.cat.consumer.business.model.IVisitor;

public class BusinessReport extends BaseEntity<BusinessReport> {
   private String m_domain;

   private java.util.Date m_startTime;

   private java.util.Date m_endTime;

   private Map<String, BusinessItem> m_businessItems = new LinkedHashMap<String, BusinessItem>();

   public BusinessReport() {
   }

   public BusinessReport(String domain) {
      m_domain = domain;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitBusinessReport(this);
   }

   public BusinessReport addBusinessItem(BusinessItem businessItem) {
      m_businessItems.put(businessItem.getId(), businessItem);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof BusinessReport) {
         BusinessReport _o = (BusinessReport) obj;

         if (!equals(getDomain(), _o.getDomain())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public BusinessItem findBusinessItem(String id) {
      return m_businessItems.get(id);
   }

   public BusinessItem findOrCreateBusinessItem(String id) {
      BusinessItem businessItem = m_businessItems.get(id);

      if (businessItem == null) {
         synchronized (m_businessItems) {
            businessItem = m_businessItems.get(id);

            if (businessItem == null) {
               businessItem = new BusinessItem(id);
               m_businessItems.put(id, businessItem);
            }
         }
      }

      return businessItem;
   }

   public Map<String, BusinessItem> getBusinessItems() {
      return m_businessItems;
   }

   public String getDomain() {
      return m_domain;
   }

   public java.util.Date getEndTime() {
      return m_endTime;
   }

   public java.util.Date getStartTime() {
      return m_startTime;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_domain == null ? 0 : m_domain.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(BusinessReport other) {
      assertAttributeEquals(other, ENTITY_BUSINESS_REPORT, ATTR_DOMAIN, m_domain, other.getDomain());

      if (other.getStartTime() != null) {
         m_startTime = other.getStartTime();
      }

      if (other.getEndTime() != null) {
         m_endTime = other.getEndTime();
      }
   }

   public BusinessItem removeBusinessItem(String id) {
      return m_businessItems.remove(id);
   }

   public BusinessReport setDomain(String domain) {
      m_domain = domain;
      return this;
   }

   public BusinessReport setEndTime(java.util.Date endTime) {
      m_endTime = endTime;
      return this;
   }

   public BusinessReport setStartTime(java.util.Date startTime) {
      m_startTime = startTime;
      return this;
   }

}
