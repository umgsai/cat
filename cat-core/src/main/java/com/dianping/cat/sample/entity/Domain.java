package com.dianping.cat.sample.entity;

import static com.dianping.cat.sample.Constants.ATTR_ID;
import static com.dianping.cat.sample.Constants.ENTITY_DOMAIN;

import com.dianping.cat.sample.BaseEntity;
import com.dianping.cat.sample.IVisitor;

public class Domain extends BaseEntity<Domain> {
   private String m_id;

   private Double m_sample;

   public Domain() {
   }

   public Domain(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitDomain(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Domain) {
         Domain _o = (Domain) obj;

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

   public Double getSample() {
      return m_sample;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(Domain other) {
      assertAttributeEquals(other, ENTITY_DOMAIN, ATTR_ID, m_id, other.getId());

      if (other.getSample() != null) {
         m_sample = other.getSample();
      }
   }

   public Domain setId(String id) {
      m_id = id;
      return this;
   }

   public Domain setSample(Double sample) {
      m_sample = sample;
      return this;
   }

}
