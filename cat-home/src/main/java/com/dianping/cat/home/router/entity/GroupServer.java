package com.dianping.cat.home.router.entity;

import static com.dianping.cat.home.router.Constants.ATTR_ID;
import static com.dianping.cat.home.router.Constants.ENTITY_GROUP_SERVER;

import com.dianping.cat.home.router.BaseEntity;
import com.dianping.cat.home.router.IVisitor;

public class GroupServer extends BaseEntity<GroupServer> {
   private String m_id;

   public GroupServer() {
   }

   public GroupServer(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitGroupServer(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof GroupServer) {
         GroupServer _o = (GroupServer) obj;

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

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(GroupServer other) {
      assertAttributeEquals(other, ENTITY_GROUP_SERVER, ATTR_ID, m_id, other.getId());

   }

   public GroupServer setId(String id) {
      m_id = id;
      return this;
   }

}
