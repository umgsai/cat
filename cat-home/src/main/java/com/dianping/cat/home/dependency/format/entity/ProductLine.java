package com.dianping.cat.home.dependency.format.entity;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.home.dependency.format.BaseEntity;
import com.dianping.cat.home.dependency.format.IVisitor;

public class ProductLine extends BaseEntity<ProductLine> {
   private String m_id;

   private Integer m_colInside;

   private List<Domain> m_domains = new ArrayList<Domain>();

   public ProductLine() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitProductLine(this);
   }

   public ProductLine addDomain(Domain domain) {
      m_domains.add(domain);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ProductLine) {
         ProductLine _o = (ProductLine) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         if (!equals(getColInside(), _o.getColInside())) {
            return false;
         }

         if (!equals(getDomains(), _o.getDomains())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public Integer getColInside() {
      return m_colInside;
   }

   public List<Domain> getDomains() {
      return m_domains;
   }

   public String getId() {
      return m_id;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
      hash = hash * 31 + (m_colInside == null ? 0 : m_colInside.hashCode());
      for (Domain e : m_domains) {
         hash = hash * 31 + (e == null ? 0 :e.hashCode());
      }


      return hash;
   }

   @Override
   public void mergeAttributes(ProductLine other) {
      if (other.getId() != null) {
         m_id = other.getId();
      }

      if (other.getColInside() != null) {
         m_colInside = other.getColInside();
      }
   }

   public ProductLine setColInside(Integer colInside) {
      m_colInside = colInside;
      return this;
   }

   public ProductLine setId(String id) {
      m_id = id;
      return this;
   }

}
