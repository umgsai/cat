package com.dianping.cat.home.server.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.server.BaseEntity;
import com.dianping.cat.home.server.IVisitor;

public class ServerMetricConfig extends BaseEntity<ServerMetricConfig> {
   private Map<String, Group> m_groups = new LinkedHashMap<String, Group>();

   public ServerMetricConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitServerMetricConfig(this);
   }

   public ServerMetricConfig addGroup(Group group) {
      m_groups.put(group.getId(), group);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ServerMetricConfig) {
         ServerMetricConfig _o = (ServerMetricConfig) obj;

         if (!equals(getGroups(), _o.getGroups())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public Group findGroup(String id) {
      return m_groups.get(id);
   }

   public Group findOrCreateGroup(String id) {
      Group group = m_groups.get(id);

      if (group == null) {
         synchronized (m_groups) {
            group = m_groups.get(id);

            if (group == null) {
               group = new Group(id);
               m_groups.put(id, group);
            }
         }
      }

      return group;
   }

   public Map<String, Group> getGroups() {
      return m_groups;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_groups == null ? 0 : m_groups.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(ServerMetricConfig other) {
   }

   public Group removeGroup(String id) {
      return m_groups.remove(id);
   }

}
