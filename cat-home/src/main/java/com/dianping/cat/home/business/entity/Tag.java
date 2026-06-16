package com.dianping.cat.home.business.entity;

import static com.dianping.cat.home.business.Constants.ATTR_ID;
import static com.dianping.cat.home.business.Constants.ENTITY_TAG;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.home.business.BaseEntity;
import com.dianping.cat.home.business.IVisitor;

public class Tag extends BaseEntity<Tag> {
   private String m_id;

   private List<BusinessItem> m_businessItems = new ArrayList<BusinessItem>();

   public Tag() {
   }

   public Tag(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitTag(this);
   }

   public Tag addBusinessItem(BusinessItem businessItem) {
      m_businessItems.add(businessItem);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Tag) {
         Tag _o = (Tag) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public List<BusinessItem> getBusinessItems() {
      return m_businessItems;
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
   public void mergeAttributes(Tag other) {
      assertAttributeEquals(other, ENTITY_TAG, ATTR_ID, m_id, other.getId());

   }

   public Tag setId(String id) {
      m_id = id;
      return this;
   }

}
