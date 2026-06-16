package com.dianping.cat.configuration.message.transform;

import static com.dianping.cat.configuration.message.Constants.ATTR_ID;
import static com.dianping.cat.configuration.message.Constants.ATTR_NAME;
import static com.dianping.cat.configuration.message.Constants.ATTR_VALUE;

import org.xml.sax.Attributes;

import com.dianping.cat.configuration.message.entity.AtomicMessageConfig;
import com.dianping.cat.configuration.message.entity.Domain;
import com.dianping.cat.configuration.message.entity.Property;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public AtomicMessageConfig buildAtomicMessageConfig(Attributes attributes) {
      AtomicMessageConfig atomicMessageConfig = new AtomicMessageConfig();

      return atomicMessageConfig;
   }

   @Override
   public Domain buildDomain(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      Domain domain = new Domain(id);

      return domain;
   }

   @Override
   public Property buildProperty(Attributes attributes) {
      String name = attributes.getValue(ATTR_NAME);
      String value = attributes.getValue(ATTR_VALUE);
      Property property = new Property(name);

      if (value != null) {
         property.setValue(value);
      }

      return property;
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
