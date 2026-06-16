package com.dianping.cat.configuration.server.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.configuration.server.BaseEntity;
import com.dianping.cat.configuration.server.IVisitor;

public class LongConfig extends BaseEntity<LongConfig> {
   private int m_defaultUrlThreshold = 1000;

   private int m_defaultSqlThreshold = 100;

   private Map<String, Domain> m_domains = new LinkedHashMap<String, Domain>();

   private int m_defaultServiceThreshold = 50;

   public LongConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitLongConfig(this);
   }

   public LongConfig addDomain(Domain domain) {
      m_domains.put(domain.getName(), domain);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof LongConfig) {
         LongConfig _o = (LongConfig) obj;

         if (getDefaultUrlThreshold() != _o.getDefaultUrlThreshold()) {
            return false;
         }

         if (getDefaultSqlThreshold() != _o.getDefaultSqlThreshold()) {
            return false;
         }

         if (!equals(getDomains(), _o.getDomains())) {
            return false;
         }

         if (getDefaultServiceThreshold() != _o.getDefaultServiceThreshold()) {
            return false;
         }


         return true;
      }

      return false;
   }

   public Domain findDomain(String name) {
      return m_domains.get(name);
   }

   public int getDefaultServiceThreshold() {
      return m_defaultServiceThreshold;
   }

   public int getDefaultSqlThreshold() {
      return m_defaultSqlThreshold;
   }

   public int getDefaultUrlThreshold() {
      return m_defaultUrlThreshold;
   }

   public Map<String, Domain> getDomains() {
      return m_domains;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + m_defaultUrlThreshold;
      hash = hash * 31 + m_defaultSqlThreshold;
      hash = hash * 31 + (m_domains == null ? 0 : m_domains.hashCode());
      hash = hash * 31 + m_defaultServiceThreshold;

      return hash;
   }

   @Override
   public void mergeAttributes(LongConfig other) {
      m_defaultUrlThreshold = other.getDefaultUrlThreshold();

      m_defaultSqlThreshold = other.getDefaultSqlThreshold();

      m_defaultServiceThreshold = other.getDefaultServiceThreshold();
   }

   public Domain removeDomain(String name) {
      return m_domains.remove(name);
   }

   public LongConfig setDefaultServiceThreshold(int defaultServiceThreshold) {
      m_defaultServiceThreshold = defaultServiceThreshold;
      return this;
   }

   public LongConfig setDefaultSqlThreshold(int defaultSqlThreshold) {
      m_defaultSqlThreshold = defaultSqlThreshold;
      return this;
   }

   public LongConfig setDefaultUrlThreshold(int defaultUrlThreshold) {
      m_defaultUrlThreshold = defaultUrlThreshold;
      return this;
   }

}
