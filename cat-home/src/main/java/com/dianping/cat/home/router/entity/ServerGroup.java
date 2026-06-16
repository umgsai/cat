package com.dianping.cat.home.router.entity;

import static com.dianping.cat.home.router.Constants.ATTR_ID;
import static com.dianping.cat.home.router.Constants.ENTITY_SERVER_GROUP;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.router.BaseEntity;
import com.dianping.cat.home.router.IVisitor;

public class ServerGroup extends BaseEntity<ServerGroup> {
   private String m_id;

   private String m_title;

   private Map<String, GroupServer> m_groupServers = new LinkedHashMap<String, GroupServer>();

   public ServerGroup() {
   }

   public ServerGroup(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitServerGroup(this);
   }

   public ServerGroup addGroupServer(GroupServer groupServer) {
      m_groupServers.put(groupServer.getId(), groupServer);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ServerGroup) {
         ServerGroup _o = (ServerGroup) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public GroupServer findGroupServer(String id) {
      return m_groupServers.get(id);
   }

   public GroupServer findOrCreateGroupServer(String id) {
      GroupServer groupServer = m_groupServers.get(id);

      if (groupServer == null) {
         synchronized (m_groupServers) {
            groupServer = m_groupServers.get(id);

            if (groupServer == null) {
               groupServer = new GroupServer(id);
               m_groupServers.put(id, groupServer);
            }
         }
      }

      return groupServer;
   }

   public Map<String, GroupServer> getGroupServers() {
      return m_groupServers;
   }

   public String getId() {
      return m_id;
   }

   public String getTitle() {
      return m_title;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(ServerGroup other) {
      assertAttributeEquals(other, ENTITY_SERVER_GROUP, ATTR_ID, m_id, other.getId());

      if (other.getTitle() != null) {
         m_title = other.getTitle();
      }
   }

   public GroupServer removeGroupServer(String id) {
      return m_groupServers.remove(id);
   }

   public ServerGroup setId(String id) {
      m_id = id;
      return this;
   }

   public ServerGroup setTitle(String title) {
      m_title = title;
      return this;
   }

}
