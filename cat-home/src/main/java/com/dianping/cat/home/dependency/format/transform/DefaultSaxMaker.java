package com.dianping.cat.home.dependency.format.transform;

import static com.dianping.cat.home.dependency.format.Constants.ATTR_COLINSIDE;
import static com.dianping.cat.home.dependency.format.Constants.ATTR_ID;

import org.xml.sax.Attributes;

import com.dianping.cat.home.dependency.format.entity.Domain;
import com.dianping.cat.home.dependency.format.entity.ProductLine;
import com.dianping.cat.home.dependency.format.entity.TopoGraphFormatConfig;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public Domain buildDomain(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String colInside = attributes.getValue(ATTR_COLINSIDE);
      Domain domain = new Domain();

      if (id != null) {
         domain.setId(id);
      }

      if (colInside != null) {
         domain.setColInside(convert(Integer.class, colInside, null));
      }

      return domain;
   }

   @Override
   public ProductLine buildProductLine(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String colInside = attributes.getValue(ATTR_COLINSIDE);
      ProductLine productLine = new ProductLine();

      if (id != null) {
         productLine.setId(id);
      }

      if (colInside != null) {
         productLine.setColInside(convert(Integer.class, colInside, null));
      }

      return productLine;
   }

   @Override
   public TopoGraphFormatConfig buildTopoGraphFormatConfig(Attributes attributes) {
      TopoGraphFormatConfig topoGraphFormatConfig = new TopoGraphFormatConfig();

      return topoGraphFormatConfig;
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
