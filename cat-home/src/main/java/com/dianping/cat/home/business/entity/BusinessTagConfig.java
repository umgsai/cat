package com.dianping.cat.home.business.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.business.BaseEntity;
import com.dianping.cat.home.business.IVisitor;

public class BusinessTagConfig extends BaseEntity<BusinessTagConfig> {
   private Map<String, Tag> m_tags = new LinkedHashMap<String, Tag>();

   public BusinessTagConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitBusinessTagConfig(this);
   }

   public BusinessTagConfig addTag(Tag tag) {
      m_tags.put(tag.getId(), tag);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof BusinessTagConfig) {
         BusinessTagConfig _o = (BusinessTagConfig) obj;

         if (!equals(getTags(), _o.getTags())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public Tag findTag(String id) {
      return m_tags.get(id);
   }

   public Map<String, Tag> getTags() {
      return m_tags;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_tags == null ? 0 : m_tags.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(BusinessTagConfig other) {
   }

   public Tag removeTag(String id) {
      return m_tags.remove(id);
   }

}
