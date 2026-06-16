package com.dianping.cat.home.group.entity;

import static com.dianping.cat.home.group.Constants.ATTR_ID;
import static com.dianping.cat.home.group.Constants.ENTITY_DOMAIN;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.group.BaseEntity;
import com.dianping.cat.home.group.IVisitor;

public class Domain extends BaseEntity<Domain> {
   private String m_id;

   private Map<String, Group> m_groups = new LinkedHashMap<String, Group>();

   public Domain() {
   }

   public Domain(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitDomain(this);
   }

   public Domain addGroup(Group group) {
      m_groups.put(group.getId(), group);
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

   public Group findGroup(String id) {
      return m_groups.get(id);
   }

   public Map<String, Group> getGroups() {
      return m_groups;
   }

   public String getId() {
      return m_id;
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

   public Group removeGroup(String id) {
      return m_groups.remove(id);
   }

   public Domain setId(String id) {
      m_id = id;
      return this;
   }

}
