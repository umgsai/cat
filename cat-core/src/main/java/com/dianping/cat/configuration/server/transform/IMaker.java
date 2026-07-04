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

   ConsumerConfig buildConsumer(T node);

   Domain buildDomain(T node);

   HarfsConfig buildHarfs(T node);

   HdfsConfig buildHdfs(T node);

   LongConfig buildLongConfig(T node);

   Property buildProperty(T node);

   Server buildServer(T node);

   ServerConfig buildServerConfig(T node);

   StorageConfig buildStorage(T node);
}
