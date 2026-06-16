package com.dianping.cat.home.server.transform;

import static com.dianping.cat.home.server.Constants.ATTR_CATEGORY;
import static com.dianping.cat.home.server.Constants.ATTR_ID;
import static com.dianping.cat.home.server.Constants.ATTR_TYPE;

import org.xml.sax.Attributes;

import com.dianping.cat.home.server.entity.Group;
import com.dianping.cat.home.server.entity.Item;
import com.dianping.cat.home.server.entity.Segment;
import com.dianping.cat.home.server.entity.ServerMetricConfig;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public Group buildGroup(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      Group group = new Group(id);

      return group;
   }

   @Override
   public Item buildItem(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      Item item = new Item(id);

      return item;
   }

   @Override
   public Segment buildSegment(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String category = attributes.getValue(ATTR_CATEGORY);
      String type = attributes.getValue(ATTR_TYPE);
      Segment segment = new Segment(id);

      if (category != null) {
         segment.setCategory(category);
      }

      if (type != null) {
         segment.setType(type);
      }

      return segment;
   }

   @Override
   public ServerMetricConfig buildServerMetricConfig(Attributes attributes) {
      ServerMetricConfig serverMetricConfig = new ServerMetricConfig();

      return serverMetricConfig;
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
