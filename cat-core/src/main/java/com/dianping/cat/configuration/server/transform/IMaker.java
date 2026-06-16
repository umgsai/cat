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

public interface IMaker<T> {

   public ConsumerConfig buildConsumer(T node);

   public Domain buildDomain(T node);

   public HarfsConfig buildHarfs(T node);

   public HdfsConfig buildHdfs(T node);

   public LongConfig buildLongConfig(T node);

   public Property buildProperty(T node);

   public Server buildServer(T node);

   public ServerConfig buildServerConfig(T node);

   public StorageConfig buildStorage(T node);
}
