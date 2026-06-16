package com.dianping.cat.configuration.business.transform;

import static com.dianping.cat.configuration.business.Constants.ATTR_ALARM;
import static com.dianping.cat.configuration.business.Constants.ATTR_ID;
import static com.dianping.cat.configuration.business.Constants.ATTR_PRIVILEGE;
import static com.dianping.cat.configuration.business.Constants.ATTR_SHOW_AVG;
import static com.dianping.cat.configuration.business.Constants.ATTR_SHOW_COUNT;
import static com.dianping.cat.configuration.business.Constants.ATTR_SHOW_SUM;
import static com.dianping.cat.configuration.business.Constants.ATTR_TITLE;
import static com.dianping.cat.configuration.business.Constants.ATTR_VIEW_ORDER;

import org.xml.sax.Attributes;

import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public BusinessItemConfig buildBusinessItemConfig(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String viewOrder = attributes.getValue(ATTR_VIEW_ORDER);
      String title = attributes.getValue(ATTR_TITLE);
      String showCount = attributes.getValue(ATTR_SHOW_COUNT);
      String showAvg = attributes.getValue(ATTR_SHOW_AVG);
      String showSum = attributes.getValue(ATTR_SHOW_SUM);
      String alarm = attributes.getValue(ATTR_ALARM);
      String privilege = attributes.getValue(ATTR_PRIVILEGE);
      BusinessItemConfig businessItemConfig = new BusinessItemConfig(id);

      if (viewOrder != null) {
         businessItemConfig.setViewOrder(convert(Double.class, viewOrder, 0.0));
      }

      if (title != null) {
         businessItemConfig.setTitle(title);
      }

      if (showCount != null) {
         businessItemConfig.setShowCount(convert(Boolean.class, showCount, false));
      }

      if (showAvg != null) {
         businessItemConfig.setShowAvg(convert(Boolean.class, showAvg, false));
      }

      if (showSum != null) {
         businessItemConfig.setShowSum(convert(Boolean.class, showSum, false));
      }

      if (alarm != null) {
         businessItemConfig.setAlarm(convert(Boolean.class, alarm, false));
      }

      if (privilege != null) {
         businessItemConfig.setPrivilege(convert(Boolean.class, privilege, false));
      }

      return businessItemConfig;
   }

   @Override
   public BusinessReportConfig buildBusinessReportConfig(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      BusinessReportConfig businessReportConfig = new BusinessReportConfig();

      if (id != null) {
         businessReportConfig.setId(id);
      }

      return businessReportConfig;
   }

   @Override
   public CustomConfig buildCustomConfig(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String viewOrder = attributes.getValue(ATTR_VIEW_ORDER);
      String title = attributes.getValue(ATTR_TITLE);
      String alarm = attributes.getValue(ATTR_ALARM);
      String privilege = attributes.getValue(ATTR_PRIVILEGE);
      CustomConfig customConfig = new CustomConfig(id);

      if (viewOrder != null) {
         customConfig.setViewOrder(convert(Double.class, viewOrder, 0.0));
      }

      if (title != null) {
         customConfig.setTitle(title);
      }

      if (alarm != null) {
         customConfig.setAlarm(convert(Boolean.class, alarm, false));
      }

      if (privilege != null) {
         customConfig.setPrivilege(convert(Boolean.class, privilege, false));
      }

      return customConfig;
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
