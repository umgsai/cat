package com.dianping.cat.configuration.tp.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.configuration.tp.BaseEntity;
import com.dianping.cat.configuration.tp.IVisitor;

public class TpValueStatisticConfig extends BaseEntity<TpValueStatisticConfig> {
   private Map<String, Domain> m_domains = new LinkedHashMap<String, Domain>();

   public TpValueStatisticConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitTpValueStatisticConfig(this);
   }

   public TpValueStatisticConfig addDomain(Domain domain) {
      m_domains.put(domain.getId(), domain);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof TpValueStatisticConfig) {
         TpValueStatisticConfig _o = (TpValueStatisticConfig) obj;

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

   public Domain findOrCreateDomain(String id) {
      Domain domain = m_domains.get(id);

      if (domain == null) {
         synchronized (m_domains) {
            domain = m_domains.get(id);

            if (domain == null) {
               domain = new Domain(id);
               m_domains.put(id, domain);
            }
         }
      }

      return domain;
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
   public void mergeAttributes(TpValueStatisticConfig other) {
   }

   public Domain removeDomain(String id) {
      return m_domains.remove(id);
   }

}
