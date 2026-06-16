package com.dianping.cat.consumer.business.model.transform;

import static com.dianping.cat.consumer.business.model.Constants.ATTR_AVG;
import static com.dianping.cat.consumer.business.model.Constants.ATTR_COUNT;
import static com.dianping.cat.consumer.business.model.Constants.ATTR_DOMAIN;
import static com.dianping.cat.consumer.business.model.Constants.ATTR_ENDTIME;
import static com.dianping.cat.consumer.business.model.Constants.ATTR_ID;
import static com.dianping.cat.consumer.business.model.Constants.ATTR_STARTTIME;
import static com.dianping.cat.consumer.business.model.Constants.ATTR_SUM;
import static com.dianping.cat.consumer.business.model.Constants.ATTR_TYPE;

import org.xml.sax.Attributes;

import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public BusinessItem buildBusinessItem(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String type = attributes.getValue(ATTR_TYPE);
      BusinessItem businessItem = new BusinessItem(id);

      if (type != null) {
         businessItem.setType(type);
      }

      return businessItem;
   }

   @Override
   public BusinessReport buildBusinessReport(Attributes attributes) {
      String domain = attributes.getValue(ATTR_DOMAIN);
      String startTime = attributes.getValue(ATTR_STARTTIME);
      String endTime = attributes.getValue(ATTR_ENDTIME);
      BusinessReport businessReport = new BusinessReport(domain);

      if (startTime != null) {
         businessReport.setStartTime(toDate(startTime, "yyyy-MM-dd HH:mm:ss", null));
      }

      if (endTime != null) {
         businessReport.setEndTime(toDate(endTime, "yyyy-MM-dd HH:mm:ss", null));
      }

      return businessReport;
   }

   @Override
   public Segment buildSegment(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String count = attributes.getValue(ATTR_COUNT);
      String sum = attributes.getValue(ATTR_SUM);
      String avg = attributes.getValue(ATTR_AVG);
      Segment segment = new Segment(id == null ? null : convert(Integer.class, id, null));

      if (count != null) {
         segment.setCount(convert(Integer.class, count, 0));
      }

      if (sum != null) {
         segment.setSum(convert(Double.class, sum, 0.0));
      }

      if (avg != null) {
         segment.setAvg(convert(Double.class, avg, 0.0));
      }

      return segment;
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
