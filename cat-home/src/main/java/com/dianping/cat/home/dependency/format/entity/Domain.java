package com.dianping.cat.home.dependency.format.entity;

import com.dianping.cat.home.dependency.format.BaseEntity;
import com.dianping.cat.home.dependency.format.IVisitor;

public class Domain extends BaseEntity<Domain> {
   private String m_id;

   private Integer m_colInside;

   public Domain() {
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

         if (!equals(getColInside(), _o.getColInside())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public Integer getColInside() {
      return m_colInside;
   }

   public String getId() {
      return m_id;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
      hash = hash * 31 + (m_colInside == null ? 0 : m_colInside.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(Domain other) {
      if (other.getId() != null) {
         m_id = other.getId();
      }

      if (other.getColInside() != null) {
         m_colInside = other.getColInside();
      }
   }

   public Domain setColInside(Integer colInside) {
      m_colInside = colInside;
      return this;
   }

   public Domain setId(String id) {
      m_id = id;
      return this;
   }

}
