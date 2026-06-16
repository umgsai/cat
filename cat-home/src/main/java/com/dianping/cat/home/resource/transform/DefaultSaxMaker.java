package com.dianping.cat.home.resource.transform;

import static com.dianping.cat.home.resource.Constants.ATTR_OP;
import static com.dianping.cat.home.resource.Constants.ATTR_PATH;
import static com.dianping.cat.home.resource.Constants.ATTR_ROLE;

import org.xml.sax.Attributes;

import com.dianping.cat.home.resource.entity.Resource;
import com.dianping.cat.home.resource.entity.ResourceConfig;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public Resource buildResource(Attributes attributes) {
      String path = attributes.getValue(ATTR_PATH);
      String op = attributes.getValue(ATTR_OP);
      String role = attributes.getValue(ATTR_ROLE);
      Resource resource = new Resource();

      if (path != null) {
         resource.setPath(path);
      }

      if (op != null) {
         resource.setOp(op);
      }

      if (role != null) {
         resource.setRole(convert(Integer.class, role, 0));
      }

      return resource;
   }

   @Override
   public ResourceConfig buildResourceConfig(Attributes attributes) {
      ResourceConfig resourceConfig = new ResourceConfig();

      return resourceConfig;
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
