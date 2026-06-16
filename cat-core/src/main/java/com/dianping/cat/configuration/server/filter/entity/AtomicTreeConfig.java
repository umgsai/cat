package com.dianping.cat.configuration.server.filter.entity;

import com.dianping.cat.configuration.server.filter.BaseEntity;
import com.dianping.cat.configuration.server.filter.IVisitor;

public class AtomicTreeConfig extends BaseEntity<AtomicTreeConfig> {
   private String m_startTypes;

   private String m_matchTypes;

   public AtomicTreeConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitAtomicTreeConfig(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof AtomicTreeConfig) {
         AtomicTreeConfig _o = (AtomicTreeConfig) obj;

         if (!equals(getStartTypes(), _o.getStartTypes())) {
            return false;
         }

         if (!equals(getMatchTypes(), _o.getMatchTypes())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public String getMatchTypes() {
      return m_matchTypes;
   }

   public String getStartTypes() {
      return m_startTypes;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_startTypes == null ? 0 : m_startTypes.hashCode());
      hash = hash * 31 + (m_matchTypes == null ? 0 : m_matchTypes.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(AtomicTreeConfig other) {
      if (other.getStartTypes() != null) {
         m_startTypes = other.getStartTypes();
      }

      if (other.getMatchTypes() != null) {
         m_matchTypes = other.getMatchTypes();
      }
   }

   public AtomicTreeConfig setMatchTypes(String matchTypes) {
      m_matchTypes = matchTypes;
      return this;
   }

   public AtomicTreeConfig setStartTypes(String startTypes) {
      m_startTypes = startTypes;
      return this;
   }

}
