package com.dianping.cat.configuration.server.transform;

import static com.dianping.cat.configuration.server.Constants.ATTR_BASE_DIR;
import static com.dianping.cat.configuration.server.Constants.ATTR_DEFAULT_SERVICE_THRESHOLD;
import static com.dianping.cat.configuration.server.Constants.ATTR_DEFAULT_SQL_THRESHOLD;
import static com.dianping.cat.configuration.server.Constants.ATTR_DEFAULT_URL_THRESHOLD;
import static com.dianping.cat.configuration.server.Constants.ATTR_HAR_MODE;
import static com.dianping.cat.configuration.server.Constants.ATTR_ID;
import static com.dianping.cat.configuration.server.Constants.ATTR_LOCAL_BASE_DIR;
import static com.dianping.cat.configuration.server.Constants.ATTR_LOCAL_LOGIVEW_STORAGE_TIME;
import static com.dianping.cat.configuration.server.Constants.ATTR_LOCAL_REPORT_STORAGE_TIME;
import static com.dianping.cat.configuration.server.Constants.ATTR_MAX_HDFS_STORAGE_TIME;
import static com.dianping.cat.configuration.server.Constants.ATTR_MAX_SIZE;
import static com.dianping.cat.configuration.server.Constants.ATTR_NAME;
import static com.dianping.cat.configuration.server.Constants.ATTR_SERVER_URI;
import static com.dianping.cat.configuration.server.Constants.ATTR_SERVICE_THRESHOLD;
import static com.dianping.cat.configuration.server.Constants.ATTR_SQL_THRESHOLD;
import static com.dianping.cat.configuration.server.Constants.ATTR_UPLOAD_THREAD;
import static com.dianping.cat.configuration.server.Constants.ATTR_URL_THRESHOLD;
import static com.dianping.cat.configuration.server.Constants.ATTR_VALUE;

import org.xml.sax.Attributes;

import com.dianping.cat.configuration.server.entity.ConsumerConfig;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.configuration.server.entity.HarfsConfig;
import com.dianping.cat.configuration.server.entity.HdfsConfig;
import com.dianping.cat.configuration.server.entity.LongConfig;
import com.dianping.cat.configuration.server.entity.Property;
import com.dianping.cat.configuration.server.entity.Server;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.configuration.server.entity.StorageConfig;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public ConsumerConfig buildConsumer(Attributes attributes) {
      ConsumerConfig consumer = new ConsumerConfig();

      return consumer;
   }

   @Override
   public Domain buildDomain(Attributes attributes) {
      String name = attributes.getValue(ATTR_NAME);
      String urlThreshold = attributes.getValue(ATTR_URL_THRESHOLD);
      String sqlThreshold = attributes.getValue(ATTR_SQL_THRESHOLD);
      String serviceThreshold = attributes.getValue(ATTR_SERVICE_THRESHOLD);
      Domain domain = new Domain(name);

      if (urlThreshold != null) {
         domain.setUrlThreshold(convert(Integer.class, urlThreshold, null));
      }

      if (sqlThreshold != null) {
         domain.setSqlThreshold(convert(Integer.class, sqlThreshold, null));
      }

      if (serviceThreshold != null) {
         domain.setServiceThreshold(convert(Integer.class, serviceThreshold, null));
      }

      return domain;
   }

   @Override
   public HarfsConfig buildHarfs(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String maxSize = attributes.getValue(ATTR_MAX_SIZE);
      String serverUri = attributes.getValue(ATTR_SERVER_URI);
      String baseDir = attributes.getValue(ATTR_BASE_DIR);
      HarfsConfig harfs = new HarfsConfig(id);

      if (maxSize != null) {
         harfs.setMaxSize(maxSize);
      }

      if (serverUri != null) {
         harfs.setServerUri(serverUri);
      }

      if (baseDir != null) {
         harfs.setBaseDir(baseDir);
      }

      return harfs;
   }

   @Override
   public HdfsConfig buildHdfs(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String maxSize = attributes.getValue(ATTR_MAX_SIZE);
      String serverUri = attributes.getValue(ATTR_SERVER_URI);
      String baseDir = attributes.getValue(ATTR_BASE_DIR);
      HdfsConfig hdfs = new HdfsConfig(id);

      if (maxSize != null) {
         hdfs.setMaxSize(maxSize);
      }

      if (serverUri != null) {
         hdfs.setServerUri(serverUri);
      }

      if (baseDir != null) {
         hdfs.setBaseDir(baseDir);
      }

      return hdfs;
   }

   @Override
   public LongConfig buildLongConfig(Attributes attributes) {
      String defaultUrlThreshold = attributes.getValue(ATTR_DEFAULT_URL_THRESHOLD);
      String defaultSqlThreshold = attributes.getValue(ATTR_DEFAULT_SQL_THRESHOLD);
      String defaultServiceThreshold = attributes.getValue(ATTR_DEFAULT_SERVICE_THRESHOLD);
      LongConfig longConfig = new LongConfig();

      if (defaultUrlThreshold != null) {
         longConfig.setDefaultUrlThreshold(convert(Integer.class, defaultUrlThreshold, 0));
      }

      if (defaultSqlThreshold != null) {
         longConfig.setDefaultSqlThreshold(convert(Integer.class, defaultSqlThreshold, 0));
      }

      if (defaultServiceThreshold != null) {
         longConfig.setDefaultServiceThreshold(convert(Integer.class, defaultServiceThreshold, 0));
      }

      return longConfig;
   }

   @Override
   public Property buildProperty(Attributes attributes) {
      String name = attributes.getValue(ATTR_NAME);
      String value = attributes.getValue(ATTR_VALUE);
      Property property = new Property(name);

      if (value != null) {
         property.setValue(value);
      }

      return property;
   }

   @Override
   public Server buildServer(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      Server server = new Server(id);

      return server;
   }

   @Override
   public ServerConfig buildServerConfig(Attributes attributes) {
      ServerConfig serverConfig = new ServerConfig();

      return serverConfig;
   }

   @Override
   public StorageConfig buildStorage(Attributes attributes) {
      String localBaseDir = attributes.getValue(ATTR_LOCAL_BASE_DIR);
      String maxHdfsStorageTime = attributes.getValue(ATTR_MAX_HDFS_STORAGE_TIME);
      String localReportStorageTime = attributes.getValue(ATTR_LOCAL_REPORT_STORAGE_TIME);
      String localLogivewStorageTime = attributes.getValue(ATTR_LOCAL_LOGIVEW_STORAGE_TIME);
      String harMode = attributes.getValue(ATTR_HAR_MODE);
      String uploadThread = attributes.getValue(ATTR_UPLOAD_THREAD);
      StorageConfig storage = new StorageConfig();

      if (localBaseDir != null) {
         storage.setLocalBaseDir(localBaseDir);
      }

      if (maxHdfsStorageTime != null) {
         storage.setMaxHdfsStorageTime(convert(Integer.class, maxHdfsStorageTime, 0));
      }

      if (localReportStorageTime != null) {
         storage.setLocalReportStorageTime(convert(Integer.class, localReportStorageTime, 0));
      }

      if (localLogivewStorageTime != null) {
         storage.setLocalLogivewStorageTime(convert(Integer.class, localLogivewStorageTime, 0));
      }

      if (harMode != null) {
         storage.setHarMode(convert(Boolean.class, harMode, false));
      }

      if (uploadThread != null) {
         storage.setUploadThread(convert(Integer.class, uploadThread, 0));
      }

      return storage;
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
