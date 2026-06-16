package com.dianping.cat.configuration.server;

import com.dianping.cat.configuration.server.entity.ConsumerConfig;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.configuration.server.entity.HarfsConfig;
import com.dianping.cat.configuration.server.entity.HdfsConfig;
import com.dianping.cat.configuration.server.entity.LongConfig;
import com.dianping.cat.configuration.server.entity.Property;
import com.dianping.cat.configuration.server.entity.Server;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.configuration.server.entity.StorageConfig;

public interface IVisitor {

   public void visitConsumer(ConsumerConfig consumer);

   public void visitDomain(Domain domain);

   public void visitHarfs(HarfsConfig harfs);

   public void visitHdfs(HdfsConfig hdfs);

   public void visitLongConfig(LongConfig longConfig);

   public void visitProperty(Property property);

   public void visitServer(Server server);

   public void visitServerConfig(ServerConfig serverConfig);

   public void visitStorage(StorageConfig storage);
}
