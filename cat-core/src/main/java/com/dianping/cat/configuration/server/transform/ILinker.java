package com.dianping.cat.configuration.server.transform;

import com.dianping.cat.configuration.server.entity.ConsumerConfig;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.configuration.server.entity.HarfsConfig;
import com.dianping.cat.configuration.server.entity.HdfsConfig;
import com.dianping.cat.configuration.server.entity.LongConfig;
import com.dianping.cat.configuration.server.entity.Property;
import com.dianping.cat.configuration.server.entity.Server;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.configuration.server.entity.StorageConfig;

public interface ILinker {

   boolean onConsumer(Server parent, ConsumerConfig consumer);

   boolean onDomain(LongConfig parent, Domain domain);

   boolean onHarfs(StorageConfig parent, HarfsConfig harfs);

   boolean onHdfs(StorageConfig parent, HdfsConfig hdfs);

   boolean onLongConfig(ConsumerConfig parent, LongConfig longConfig);

   boolean onProperty(Server parent, Property property);

   boolean onProperty(StorageConfig parent, Property property);

   boolean onServer(ServerConfig parent, Server server);

   boolean onStorage(Server parent, StorageConfig storage);
}
