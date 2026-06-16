package com.dianping.cat.home.user.transform;

import static com.dianping.cat.home.user.Constants.ATTR_ID;
import static com.dianping.cat.home.user.Constants.ATTR_ROLE;

import org.xml.sax.Attributes;

import com.dianping.cat.home.user.entity.User;
import com.dianping.cat.home.user.entity.UserConfig;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public User buildUser(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String role = attributes.getValue(ATTR_ROLE);
      User user = new User(id);

      if (role != null) {
         user.setRole(convert(Integer.class, role, 0));
      }

      return user;
   }

   @Override
   public UserConfig buildUserConfig(Attributes attributes) {
      UserConfig userConfig = new UserConfig();

      return userConfig;
   }

   @SuppressWarnings("unchecked")
   protected <T> T convert(Class<T> type, String value, T defaultValue) {
      if (value == null || value.length() == 0) {
         return defaultValue;
      }

      if (type == Boolean.class || type == Boolean.TYPE) {
         return (T) Boolean.valueOf(value);
      } else if (type == Integer.class || type == Integer.TYPE) {
         return (T) Integer.valueOf(value);
      } else if (type == Long.class || type == Long.TYPE) {
         return (T) Long.valueOf(value);
      } else if (type == Short.class || type == Short.TYPE) {
         return (T) Short.valueOf(value);
      } else if (type == Float.class || type == Float.TYPE) {
         return (T) Float.valueOf(value);
      } else if (type == Double.class || type == Double.TYPE) {
         return (T) Double.valueOf(value);
      } else if (type == Byte.class || type == Byte.TYPE) {
         return (T) Byte.valueOf(value);
      } else if (type == Character.class || type == Character.TYPE) {
         return (T) (Character) value.charAt(0);
      } else {
         return (T) value;
      }
   }
}
