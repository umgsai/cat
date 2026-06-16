package com.dianping.cat.configuration.tp.entity;

import static com.dianping.cat.configuration.tp.Constants.ATTR_ID;
import static com.dianping.cat.configuration.tp.Constants.ENTITY_DOMAIN;

import java.util.LinkedHashSet;
import java.util.Set;

import com.dianping.cat.configuration.tp.BaseEntity;
import com.dianping.cat.configuration.tp.IVisitor;

public class Domain extends BaseEntity<Domain> {
   private String m_id;

   private Set<String> m_transactionTypes = new LinkedHashSet<String>();

   public Domain() {
   }

   public Domain(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitDomain(this);
   }

   public Domain addTransactionType(String transactionType) {
      m_transactionTypes.add(transactionType);
      return this;
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

   public Set<String> getTransactionTypes() {
      return m_transactionTypes;
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

   }

   public Domain setId(String id) {
      m_id = id;
      return this;
   }

}
