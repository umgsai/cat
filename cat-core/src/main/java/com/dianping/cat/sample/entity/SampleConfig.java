package com.dianping.cat.sample.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.sample.BaseEntity;
import com.dianping.cat.sample.IVisitor;

public class SampleConfig extends BaseEntity<SampleConfig> {
   private Map<String, Domain> m_domains = new LinkedHashMap<String, Domain>();

   public SampleConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitSampleConfig(this);
   }

   public SampleConfig addDomain(Domain domain) {
      m_domains.put(domain.getId(), domain);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof SampleConfig) {
         SampleConfig _o = (SampleConfig) obj;

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
   public void mergeAttributes(SampleConfig other) {
   }

   public Domain removeDomain(String id) {
      return m_domains.remove(id);
   }

}
