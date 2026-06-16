package com.dianping.cat.home.user.entity;

import static com.dianping.cat.home.user.Constants.ATTR_ID;
import static com.dianping.cat.home.user.Constants.ENTITY_USER;

import com.dianping.cat.home.user.BaseEntity;
import com.dianping.cat.home.user.IVisitor;

public class User extends BaseEntity<User> {
   private String m_id;

   private int m_role;

   public User() {
   }

   public User(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitUser(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof User) {
         User _o = (User) obj;

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

   public int getRole() {
      return m_role;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(User other) {
      assertAttributeEquals(other, ENTITY_USER, ATTR_ID, m_id, other.getId());

      m_role = other.getRole();
   }

   public User setId(String id) {
      m_id = id;
      return this;
   }

   public User setRole(int role) {
      m_role = role;
      return this;
   }

}
