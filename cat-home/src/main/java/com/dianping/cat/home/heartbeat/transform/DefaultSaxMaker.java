package com.dianping.cat.home.heartbeat.transform;

import static com.dianping.cat.home.heartbeat.Constants.ATTR_ALERT;
import static com.dianping.cat.home.heartbeat.Constants.ATTR_DELTA;
import static com.dianping.cat.home.heartbeat.Constants.ATTR_ID;
import static com.dianping.cat.home.heartbeat.Constants.ATTR_LABLE;
import static com.dianping.cat.home.heartbeat.Constants.ATTR_ORDER;
import static com.dianping.cat.home.heartbeat.Constants.ATTR_TITLE;
import static com.dianping.cat.home.heartbeat.Constants.ATTR_UNIT;

import org.xml.sax.Attributes;

import com.dianping.cat.home.heartbeat.entity.Group;
import com.dianping.cat.home.heartbeat.entity.HeartbeatDisplayPolicy;
import com.dianping.cat.home.heartbeat.entity.Metric;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public Group buildGroup(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String order = attributes.getValue(ATTR_ORDER);
      Group group = new Group(id);

      if (order != null) {
         group.setOrder(convert(Integer.class, order, 0));
      }

      return group;
   }

   @Override
   public HeartbeatDisplayPolicy buildHeartbeatDisplayPolicy(Attributes attributes) {
      HeartbeatDisplayPolicy heartbeatDisplayPolicy = new HeartbeatDisplayPolicy();

      return heartbeatDisplayPolicy;
   }

   @Override
   public Metric buildMetric(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String unit = attributes.getValue(ATTR_UNIT);
      String delta = attributes.getValue(ATTR_DELTA);
      String order = attributes.getValue(ATTR_ORDER);
      String title = attributes.getValue(ATTR_TITLE);
      String lable = attributes.getValue(ATTR_LABLE);
      String alert = attributes.getValue(ATTR_ALERT);
      Metric metric = new Metric(id);

      if (unit != null) {
         metric.setUnit(unit);
      }

      if (delta != null) {
         metric.setDelta(convert(Boolean.class, delta, false));
      }

      if (order != null) {
         metric.setOrder(convert(Integer.class, order, 0));
      }

      if (title != null) {
         metric.setTitle(title);
      }

      if (lable != null) {
         metric.setLable(lable);
      }

      if (alert != null) {
         metric.setAlert(convert(Boolean.class, alert, false));
      }

      return metric;
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
