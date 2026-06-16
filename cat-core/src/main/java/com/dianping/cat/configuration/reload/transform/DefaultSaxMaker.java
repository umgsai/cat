package com.dianping.cat.configuration.reload.transform;

import static com.dianping.cat.configuration.reload.Constants.ATTR_ID;

import org.xml.sax.Attributes;

import com.dianping.cat.configuration.reload.entity.ReportPeriod;
import com.dianping.cat.configuration.reload.entity.ReportReloadConfig;
import com.dianping.cat.configuration.reload.entity.ReportType;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public ReportPeriod buildReportPeriod(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      ReportPeriod reportPeriod = new ReportPeriod();

      if (id != null) {
         reportPeriod.setId(id);
      }

      return reportPeriod;
   }

   @Override
   public ReportReloadConfig buildReportReloadConfig(Attributes attributes) {
      ReportReloadConfig reportReloadConfig = new ReportReloadConfig();

      return reportReloadConfig;
   }

   @Override
   public ReportType buildReportType(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      ReportType reportType = new ReportType(id);

      return reportType;
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
