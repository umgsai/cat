package com.dianping.cat.configuration.message.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.configuration.message.BaseEntity;
import com.dianping.cat.configuration.message.IVisitor;

public class AtomicMessageConfig extends BaseEntity<AtomicMessageConfig> {
   private Map<String, Domain> m_domains = new LinkedHashMap<String, Domain>();

   public AtomicMessageConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitAtomicMessageConfig(this);
   }

   public AtomicMessageConfig addDomain(Domain domain) {
      m_domains.put(domain.getId(), domain);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof AtomicMessageConfig) {
         AtomicMessageConfig _o = (AtomicMessageConfig) obj;

         if (!equals(getDomains(), _o.getDomains())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public Domain findDomain(String id) {
      return m_domains.get(id);
   }

   public Map<String, Domain> getDomains() {
      return m_domains;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_domains == null ? 0 : m_domains.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(AtomicMessageConfig other) {
   }

   public Domain removeDomain(String id) {
      return m_domains.remove(id);
   }

}
