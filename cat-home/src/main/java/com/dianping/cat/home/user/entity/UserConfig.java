package com.dianping.cat.home.user.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.user.BaseEntity;
import com.dianping.cat.home.user.IVisitor;

public class UserConfig extends BaseEntity<UserConfig> {
   private Map<String, User> m_users = new LinkedHashMap<String, User>();

   public UserConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitUserConfig(this);
   }

   public UserConfig addUser(User user) {
      m_users.put(user.getId(), user);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof UserConfig) {
         UserConfig _o = (UserConfig) obj;

         if (!equals(getUsers(), _o.getUsers())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public User findUser(String id) {
      return m_users.get(id);
   }

   public Map<String, User> getUsers() {
      return m_users;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_users == null ? 0 : m_users.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(UserConfig other) {
   }

   public User removeUser(String id) {
      return m_users.remove(id);
   }

}
