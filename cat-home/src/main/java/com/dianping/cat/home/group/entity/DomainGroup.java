package com.dianping.cat.home.group.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.group.BaseEntity;
import com.dianping.cat.home.group.IVisitor;

public class DomainGroup extends BaseEntity<DomainGroup> {
   private Map<String, Domain> m_domains = new LinkedHashMap<String, Domain>();

   public DomainGroup() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitDomainGroup(this);
   }

   public DomainGroup addDomain(Domain domain) {
      m_domains.put(domain.getId(), domain);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof DomainGroup) {
         DomainGroup _o = (DomainGroup) obj;

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
   public void mergeAttributes(DomainGroup other) {
   }

   public Domain removeDomain(String id) {
      return m_domains.remove(id);
   }

}
