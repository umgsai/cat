package com.dianping.cat.home.graph.transform;

import static com.dianping.cat.home.graph.Constants.ATTR_CATEGORY;
import static com.dianping.cat.home.graph.Constants.ATTR_ENDPOINT;
import static com.dianping.cat.home.graph.Constants.ATTR_ID;
import static com.dianping.cat.home.graph.Constants.ATTR_MEASURE;
import static com.dianping.cat.home.graph.Constants.ATTR_TAGS;
import static com.dianping.cat.home.graph.Constants.ATTR_TYPE;
import static com.dianping.cat.home.graph.Constants.ATTR_VIEW;

import org.xml.sax.Attributes;

import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.entity.Item;
import com.dianping.cat.home.graph.entity.Segment;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public Graph buildGraph(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      Graph graph = new Graph(id);

      return graph;
   }

   @Override
   public Item buildItem(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String view = attributes.getValue(ATTR_VIEW);
      Item item = new Item(id);

      if (view != null) {
         item.setView(view);
      }

      return item;
   }

   @Override
   public Segment buildSegment(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String category = attributes.getValue(ATTR_CATEGORY);
      String measure = attributes.getValue(ATTR_MEASURE);
      String endPoint = attributes.getValue(ATTR_ENDPOINT);
      String tags = attributes.getValue(ATTR_TAGS);
      String type = attributes.getValue(ATTR_TYPE);
      Segment segment = new Segment(id);

      if (category != null) {
         segment.setCategory(category);
      }

      if (measure != null) {
         segment.setMeasure(measure);
      }

      if (endPoint != null) {
         segment.setEndPoint(endPoint);
      }

      if (tags != null) {
         segment.setTags(tags);
      }

      if (type != null) {
         segment.setType(type);
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
}
