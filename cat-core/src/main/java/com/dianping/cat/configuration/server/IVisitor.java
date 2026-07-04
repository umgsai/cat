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

   void visitConsumer(ConsumerConfig consumer);

   void visitDomain(Domain domain);

   void visitHarfs(HarfsConfig harfs);

   void visitHdfs(HdfsConfig hdfs);

   void visitLongConfig(LongConfig longConfig);

   void visitProperty(Property property);

   void visitServer(Server server);

   void visitServerConfig(ServerConfig serverConfig);

   void visitStorage(StorageConfig storage);
}
