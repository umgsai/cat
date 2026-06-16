package com.dianping.cat.home.exception.transform;

import static com.dianping.cat.home.exception.Constants.ATTR_AVAILABLE;
import static com.dianping.cat.home.exception.Constants.ATTR_DOMAIN;
import static com.dianping.cat.home.exception.Constants.ATTR_ERROR;
import static com.dianping.cat.home.exception.Constants.ATTR_ID;
import static com.dianping.cat.home.exception.Constants.ATTR_NAME;
import static com.dianping.cat.home.exception.Constants.ATTR_WARNING;

import org.xml.sax.Attributes;

import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.home.exception.entity.ExceptionRuleConfig;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public ExceptionExclude buildExceptionExclude(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String domain = attributes.getValue(ATTR_DOMAIN);
      String name = attributes.getValue(ATTR_NAME);
      ExceptionExclude exceptionExclude = new ExceptionExclude(id);

      if (domain != null) {
         exceptionExclude.setDomain(domain);
      }

      if (name != null) {
         exceptionExclude.setName(name);
      }

      return exceptionExclude;
   }

   @Override
   public ExceptionLimit buildExceptionLimit(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String domain = attributes.getValue(ATTR_DOMAIN);
      String name = attributes.getValue(ATTR_NAME);
      String warning = attributes.getValue(ATTR_WARNING);
      String error = attributes.getValue(ATTR_ERROR);
      String available = attributes.getValue(ATTR_AVAILABLE);
      ExceptionLimit exceptionLimit = new ExceptionLimit(id);

      if (domain != null) {
         exceptionLimit.setDomain(domain);
      }

      if (name != null) {
         exceptionLimit.setName(name);
      }

      if (warning != null) {
         exceptionLimit.setWarning(convert(Integer.class, warning, 0));
      }

      if (error != null) {
         exceptionLimit.setError(convert(Integer.class, error, 0));
      }

      if (available != null) {
         exceptionLimit.setAvailable(convert(Boolean.class, available, null));
      }

      return exceptionLimit;
   }

   @Override
   public ExceptionRuleConfig buildExceptionRuleConfig(Attributes attributes) {
      ExceptionRuleConfig exceptionRuleConfig = new ExceptionRuleConfig();

      return exceptionRuleConfig;
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
