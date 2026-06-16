package com.dianping.cat.configuration.server.transform;

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

public abstract class BaseVisitor implements IVisitor {
   @Override
   public void visitConsumer(ConsumerConfig consumer) {
      if (consumer.getLongConfig() != null) {
         visitLongConfig(consumer.getLongConfig());
      }
   }

   @Override
   public void visitDomain(Domain domain) {
   }

   @Override
   public void visitHarfs(HarfsConfig harfs) {
   }

   @Override
   public void visitHdfs(HdfsConfig hdfs) {
   }

   @Override
   public void visitLongConfig(LongConfig longConfig) {
      for (Domain domain : longConfig.getDomains().values()) {
         visitDomain(domain);
      }
   }

   @Override
   public void visitProperty(Property property) {
   }

   @Override
   public void visitServer(Server server) {
      for (Property property : server.getProperties().values()) {
         visitProperty(property);
      }

      if (server.getStorage() != null) {
         visitStorage(server.getStorage());
      }

      if (server.getConsumer() != null) {
         visitConsumer(server.getConsumer());
      }
   }

   @Override
   public void visitServerConfig(ServerConfig serverConfig) {
      for (Server server : serverConfig.getServers().values()) {
         visitServer(server);
      }
   }

   @Override
   public void visitStorage(StorageConfig storage) {
      for (HdfsConfig hdfs : storage.getHdfses().values()) {
         visitHdfs(hdfs);
      }

      for (HarfsConfig harfs : storage.getHarfses().values()) {
         visitHarfs(harfs);
      }

      for (Property property : storage.getProperties().values()) {
         visitProperty(property);
      }
   }
}
