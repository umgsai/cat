package com.dianping.cat.sample.transform;

import static com.dianping.cat.sample.Constants.ATTR_ID;
import static com.dianping.cat.sample.Constants.ATTR_SAMPLE;

import org.xml.sax.Attributes;

import com.dianping.cat.sample.entity.Domain;
import com.dianping.cat.sample.entity.SampleConfig;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public Domain buildDomain(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String sample = attributes.getValue(ATTR_SAMPLE);
      Domain domain = new Domain(id);

      if (sample != null) {
         domain.setSample(convert(Double.class, sample, null));
      }

      return domain;
   }

   @Override
   public SampleConfig buildSampleConfig(Attributes attributes) {
      SampleConfig sampleConfig = new SampleConfig();

      return sampleConfig;
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
