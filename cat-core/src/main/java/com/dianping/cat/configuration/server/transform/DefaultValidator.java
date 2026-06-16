package com.dianping.cat.configuration.server.transform;

import static com.dianping.cat.configuration.server.Constants.ATTR_ID;
import static com.dianping.cat.configuration.server.Constants.ATTR_NAME;
import static com.dianping.cat.configuration.server.Constants.ENTITY_CONSUMER;
import static com.dianping.cat.configuration.server.Constants.ENTITY_DOMAIN;
import static com.dianping.cat.configuration.server.Constants.ENTITY_HARFS;
import static com.dianping.cat.configuration.server.Constants.ENTITY_HDFS;
import static com.dianping.cat.configuration.server.Constants.ENTITY_LONG_CONFIG;
import static com.dianping.cat.configuration.server.Constants.ENTITY_PROPERTIES;
import static com.dianping.cat.configuration.server.Constants.ENTITY_PROPERTY;
import static com.dianping.cat.configuration.server.Constants.ENTITY_SERVER;
import static com.dianping.cat.configuration.server.Constants.ENTITY_SERVER_CONFIG;
import static com.dianping.cat.configuration.server.Constants.ENTITY_STORAGE;

import java.util.Stack;

import com.dianping.cat.configuration.server.IVisitor;
import com.dianping.cat.configuration.server.entity.ConsumerConfig;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.configuration.server.entity.HarfsConfig;
import com.dianping.cat.configuration.server.entity.HdfsConfig;
import com.dianping.cat.configuration.server.entity.LongConfig;
import com.dianping.cat.configuration.server.entity.Property;
import com.dianping.cat.configuration.server.entity.Server;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.configuration.server.entity.StorageConfig;

public class DefaultValidator implements IVisitor {

   private Path m_path = new Path();
   
   protected void assertRequired(String name, Object value) {
      if (value == null) {
         throw new RuntimeException(String.format("%s at path(%s) is required!", name, m_path));
      }
   }

   @Override
   public void visitConsumer(ConsumerConfig consumer) {
      m_path.down(ENTITY_CONSUMER);

      visitConsumerChildren(consumer);

      m_path.up(ENTITY_CONSUMER);
   }

   protected void visitConsumerChildren(ConsumerConfig consumer) {
      if (consumer.getLongConfig() != null) {
         visitLongConfig(consumer.getLongConfig());
      }
   }

   @Override
   public void visitDomain(Domain domain) {
      m_path.down(ENTITY_DOMAIN);

      assertRequired(ATTR_NAME, domain.getName());

      m_path.up(ENTITY_DOMAIN);
   }

   @Override
   public void visitHarfs(HarfsConfig harfs) {
      m_path.down(ENTITY_HARFS);

      assertRequired(ATTR_ID, harfs.getId());

      m_path.up(ENTITY_HARFS);
   }

   @Override
   public void visitHdfs(HdfsConfig hdfs) {
      m_path.down(ENTITY_HDFS);

      assertRequired(ATTR_ID, hdfs.getId());

      m_path.up(ENTITY_HDFS);
   }

   @Override
   public void visitLongConfig(LongConfig longConfig) {
      m_path.down(ENTITY_LONG_CONFIG);

      visitLongConfigChildren(longConfig);

      m_path.up(ENTITY_LONG_CONFIG);
   }

   protected void visitLongConfigChildren(LongConfig longConfig) {
      for (Domain domain : longConfig.getDomains().values()) {
         visitDomain(domain);
      }
   }

   @Override
   public void visitProperty(Property property) {
      m_path.down(ENTITY_PROPERTY);

      assertRequired(ATTR_NAME, property.getName());

      m_path.up(ENTITY_PROPERTY);
   }

   @Override
   public void visitServer(Server server) {
      m_path.down(ENTITY_SERVER);

      assertRequired(ATTR_ID, server.getId());

      visitServerChildren(server);

      m_path.up(ENTITY_SERVER);
   }

   protected void visitServerChildren(Server server) {
      m_path.down(ENTITY_PROPERTIES);

      for (Property property : server.getProperties().values()) {
         visitProperty(property);
      }

      m_path.up(ENTITY_PROPERTIES);

      if (server.getStorage() != null) {
         visitStorage(server.getStorage());
      }

      if (server.getConsumer() != null) {
         visitConsumer(server.getConsumer());
      }
   }

   @Override
   public void visitServerConfig(ServerConfig serverConfig) {
      m_path.down(ENTITY_SERVER_CONFIG);

      visitServerConfigChildren(serverConfig);

      m_path.up(ENTITY_SERVER_CONFIG);
   }

   protected void visitServerConfigChildren(ServerConfig serverConfig) {
      for (Server server : serverConfig.getServers().values()) {
         visitServer(server);
      }
   }

   @Override
   public void visitStorage(StorageConfig storage) {
      m_path.down(ENTITY_STORAGE);

      visitStorageChildren(storage);

      m_path.up(ENTITY_STORAGE);
   }

   protected void visitStorageChildren(StorageConfig storage) {
      for (HdfsConfig hdfs : storage.getHdfses().values()) {
         visitHdfs(hdfs);
      }

      for (HarfsConfig harfs : storage.getHarfses().values()) {
         visitHarfs(harfs);
      }

      m_path.down(ENTITY_PROPERTIES);

      for (Property property : storage.getProperties().values()) {
         visitProperty(property);
      }

      m_path.up(ENTITY_PROPERTIES);
   }

   static class Path {
      private Stack<String> m_sections = new Stack<String>();

      public Path down(String nextSection) {
         m_sections.push(nextSection);

         return this;
      }

      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder();

         for (String section : m_sections) {
            sb.append('/').append(section);
         }

         return sb.toString();
      }

      public Path up(String currentSection) {
         if (m_sections.isEmpty() || !m_sections.peek().equals(currentSection)) {
            throw new RuntimeException("INTERNAL ERROR: stack mismatched!");
         }

         m_sections.pop();
         return this;
      }
   }
}
