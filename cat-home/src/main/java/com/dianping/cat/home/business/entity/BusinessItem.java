package com.dianping.cat.home.business.entity;

import com.dianping.cat.home.business.BaseEntity;
import com.dianping.cat.home.business.IVisitor;

public class BusinessItem extends BaseEntity<BusinessItem> {
   private String m_domain;

   private String m_itemId;

   public BusinessItem() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitBusinessItem(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof BusinessItem) {
         BusinessItem _o = (BusinessItem) obj;

         if (!equals(getDomain(), _o.getDomain())) {
            return false;
         }

         if (!equals(getItemId(), _o.getItemId())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public String getDomain() {
      return m_domain;
   }

   public String getItemId() {
      return m_itemId;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_domain == null ? 0 : m_domain.hashCode());
      hash = hash * 31 + (m_itemId == null ? 0 : m_itemId.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(BusinessItem other) {
      if (other.getDomain() != null) {
         m_domain = other.getDomain();
      }

      if (other.getItemId() != null) {
         m_itemId = other.getItemId();
      }
   }

   public BusinessItem setDomain(String domain) {
      m_domain = domain;
      return this;
   }

   public BusinessItem setItemId(String itemId) {
      m_itemId = itemId;
      return this;
   }

}
