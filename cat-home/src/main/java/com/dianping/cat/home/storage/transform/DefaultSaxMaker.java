package com.dianping.cat.home.storage.transform;

import static com.dianping.cat.home.storage.Constants.ATTR_DEPARTMENT;
import static com.dianping.cat.home.storage.Constants.ATTR_ID;
import static com.dianping.cat.home.storage.Constants.ATTR_PRODUCTLINE;
import static com.dianping.cat.home.storage.Constants.ATTR_TITLE;
import static com.dianping.cat.home.storage.Constants.ATTR_URL;

import org.xml.sax.Attributes;

import com.dianping.cat.home.storage.entity.Link;
import com.dianping.cat.home.storage.entity.Storage;
import com.dianping.cat.home.storage.entity.StorageGroup;
import com.dianping.cat.home.storage.entity.StorageGroupConfig;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public Link buildLink(Attributes attributes) {
      String url = attributes.getValue(ATTR_URL);
      Link link = new Link();

      if (url != null) {
         link.setUrl(url);
      }

      return link;
   }

   @Override
   public String buildPar(Attributes attributes) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Storage buildStorage(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String department = attributes.getValue(ATTR_DEPARTMENT);
      String productline = attributes.getValue(ATTR_PRODUCTLINE);
      String title = attributes.getValue(ATTR_TITLE);
      Storage storage = new Storage(id);

      if (department != null) {
         storage.setDepartment(department);
      }

      if (productline != null) {
         storage.setProductline(productline);
      }

      if (title != null) {
         storage.setTitle(title);
      }

      return storage;
   }

   @Override
   public StorageGroup buildStorageGroup(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      StorageGroup storageGroup = new StorageGroup(id);

      return storageGroup;
   }

   @Override
   public StorageGroupConfig buildStorageGroupConfig(Attributes attributes) {
      StorageGroupConfig storageGroupConfig = new StorageGroupConfig();

      return storageGroupConfig;
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
