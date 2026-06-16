package com.dianping.cat.home.service.client.transform;

import static com.dianping.cat.home.service.client.Constants.ATTR_AVG;
import static com.dianping.cat.home.service.client.Constants.ATTR_DOMAIN;
import static com.dianping.cat.home.service.client.Constants.ATTR_END_TIME;
import static com.dianping.cat.home.service.client.Constants.ATTR_FAILURECOUNT;
import static com.dianping.cat.home.service.client.Constants.ATTR_FAILUREPERCENT;
import static com.dianping.cat.home.service.client.Constants.ATTR_ID;
import static com.dianping.cat.home.service.client.Constants.ATTR_QPS;
import static com.dianping.cat.home.service.client.Constants.ATTR_SERVICE;
import static com.dianping.cat.home.service.client.Constants.ATTR_START_TIME;
import static com.dianping.cat.home.service.client.Constants.ATTR_SUM;
import static com.dianping.cat.home.service.client.Constants.ATTR_TIMEOUT;
import static com.dianping.cat.home.service.client.Constants.ATTR_TOTALCOUNT;

import org.xml.sax.Attributes;

import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.entity.Domain;
import com.dianping.cat.home.service.client.entity.Method;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public ClientReport buildClientReport(Attributes attributes) {
      String domain = attributes.getValue(ATTR_DOMAIN);
      String startTime = attributes.getValue(ATTR_START_TIME);
      String endTime = attributes.getValue(ATTR_END_TIME);
      ClientReport clientReport = new ClientReport(domain);

      if (startTime != null) {
         clientReport.setStartTime(toDate(startTime, "yyyy-MM-dd HH:mm:ss", null));
      }

      if (endTime != null) {
         clientReport.setEndTime(toDate(endTime, "yyyy-MM-dd HH:mm:ss", null));
      }

      return clientReport;
   }

   @Override
   public Domain buildDomain(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String totalCount = attributes.getValue(ATTR_TOTALCOUNT);
      String failureCount = attributes.getValue(ATTR_FAILURECOUNT);
      String failurePercent = attributes.getValue(ATTR_FAILUREPERCENT);
      String sum = attributes.getValue(ATTR_SUM);
      String avg = attributes.getValue(ATTR_AVG);
      Domain domain = new Domain(id);

      if (totalCount != null) {
         domain.setTotalCount(convert(Long.class, totalCount, 0L));
      }

      if (failureCount != null) {
         domain.setFailureCount(convert(Long.class, failureCount, 0L));
      }

      if (failurePercent != null) {
         domain.setFailurePercent(convert(Double.class, failurePercent, 0.0));
      }

      if (sum != null) {
         domain.setSum(convert(Double.class, sum, 0.0));
      }

      if (avg != null) {
         domain.setAvg(convert(Double.class, avg, 0.0));
      }

      return domain;
   }

   @Override
   public Method buildMethod(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String service = attributes.getValue(ATTR_SERVICE);
      String totalCount = attributes.getValue(ATTR_TOTALCOUNT);
      String failureCount = attributes.getValue(ATTR_FAILURECOUNT);
      String failurePercent = attributes.getValue(ATTR_FAILUREPERCENT);
      String sum = attributes.getValue(ATTR_SUM);
      String avg = attributes.getValue(ATTR_AVG);
      String qps = attributes.getValue(ATTR_QPS);
      String timeout = attributes.getValue(ATTR_TIMEOUT);
      Method method = new Method(id);

      if (service != null) {
         method.setService(service);
      }

      if (totalCount != null) {
         method.setTotalCount(convert(Long.class, totalCount, 0L));
      }

      if (failureCount != null) {
         method.setFailureCount(convert(Long.class, failureCount, 0L));
      }

      if (failurePercent != null) {
         method.setFailurePercent(convert(Double.class, failurePercent, 0.0));
      }

      if (sum != null) {
         method.setSum(convert(Double.class, sum, 0.0));
      }

      if (avg != null) {
         method.setAvg(convert(Double.class, avg, 0.0));
      }

      if (qps != null) {
         method.setQps(convert(Double.class, qps, 0.0));
      }

      if (timeout != null) {
         method.setTimeout(convert(Double.class, timeout, 0.0));
      }

      return method;
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

   protected java.util.Date toDate(String str, String format, java.util.Date defaultValue) {
      if (str == null || str.length() == 0) {
         return defaultValue;
      }

      try {
         return new java.text.SimpleDateFormat(format).parse(str);
      } catch (java.text.ParseException e) {
         throw new RuntimeException(String.format("Unable to parse date(%s) in format(%s)!", str, format), e);
      }
   }
}
