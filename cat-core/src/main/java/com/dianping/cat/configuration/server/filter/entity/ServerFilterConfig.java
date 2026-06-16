package com.dianping.cat.configuration.server.filter.entity;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.dianping.cat.configuration.server.filter.BaseEntity;
import com.dianping.cat.configuration.server.filter.IVisitor;

public class ServerFilterConfig extends BaseEntity<ServerFilterConfig> {
   private Set<String> m_transactionTypes = new LinkedHashSet<String>();

   private Set<String> m_transactionNames = new LinkedHashSet<String>();

   private Set<String> m_domains = new LinkedHashSet<String>();

   private Map<String, CrashLogDomain> m_crashLogDomains = new LinkedHashMap<String, CrashLogDomain>();

   private AtomicTreeConfig m_atomicTreeConfig;

   public ServerFilterConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitServerFilterConfig(this);
   }

   public ServerFilterConfig addCrashLogDomain(CrashLogDomain crashLogDomain) {
      m_crashLogDomains.put(crashLogDomain.getId(), crashLogDomain);
      return this;
   }

   public ServerFilterConfig addDomain(String domain) {
      m_domains.add(domain);
      return this;
   }

   public ServerFilterConfig addTransactionName(String transactionName) {
      m_transactionNames.add(transactionName);
      return this;
   }

   public ServerFilterConfig addTransactionType(String transactionType) {
      m_transactionTypes.add(transactionType);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ServerFilterConfig) {
         ServerFilterConfig _o = (ServerFilterConfig) obj;

         if (!equals(getTransactionTypes(), _o.getTransactionTypes())) {
            return false;
         }

         if (!equals(getTransactionNames(), _o.getTransactionNames())) {
            return false;
         }

         if (!equals(getDomains(), _o.getDomains())) {
            return false;
         }

         if (!equals(getCrashLogDomains(), _o.getCrashLogDomains())) {
            return false;
         }

         if (!equals(getAtomicTreeConfig(), _o.getAtomicTreeConfig())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public CrashLogDomain findCrashLogDomain(String id) {
      return m_crashLogDomains.get(id);
   }

   public AtomicTreeConfig getAtomicTreeConfig() {
      return m_atomicTreeConfig;
   }

   public Map<String, CrashLogDomain> getCrashLogDomains() {
      return m_crashLogDomains;
   }

   public Set<String> getDomains() {
      return m_domains;
   }

   public Set<String> getTransactionNames() {
      return m_transactionNames;
   }

   public Set<String> getTransactionTypes() {
      return m_transactionTypes;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      for (String e : m_transactionTypes) {
         hash = hash * 31 + (e == null ? 0 :e.hashCode());
      }

      for (String e : m_transactionNames) {
         hash = hash * 31 + (e == null ? 0 :e.hashCode());
      }

      for (String e : m_domains) {
         hash = hash * 31 + (e == null ? 0 :e.hashCode());
      }

      hash = hash * 31 + (m_crashLogDomains == null ? 0 : m_crashLogDomains.hashCode());
      hash = hash * 31 + (m_atomicTreeConfig == null ? 0 : m_atomicTreeConfig.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(ServerFilterConfig other) {
   }

   public CrashLogDomain removeCrashLogDomain(String id) {
      return m_crashLogDomains.remove(id);
   }

   public ServerFilterConfig setAtomicTreeConfig(AtomicTreeConfig atomicTreeConfig) {
      m_atomicTreeConfig = atomicTreeConfig;
      return this;
   }

}
