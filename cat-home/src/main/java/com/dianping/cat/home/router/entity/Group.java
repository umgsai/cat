package com.dianping.cat.home.router.entity;

import static com.dianping.cat.home.router.Constants.ATTR_ID;
import static com.dianping.cat.home.router.Constants.ENTITY_GROUP;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.home.router.BaseEntity;
import com.dianping.cat.home.router.IVisitor;

public class Group extends BaseEntity<Group> {
   private String m_id;

   private List<Server> m_servers = new ArrayList<Server>();

   public Group() {
   }

   public Group(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitGroup(this);
   }

   public Group addServer(Server server) {
      m_servers.add(server);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Group) {
         Group _o = (Group) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public String getId() {
      return m_id;
   }

   public List<Server> getServers() {
      return m_servers;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(Group other) {
      assertAttributeEquals(other, ENTITY_GROUP, ATTR_ID, m_id, other.getId());

   }

   public Group setId(String id) {
      m_id = id;
      return this;
   }

}
