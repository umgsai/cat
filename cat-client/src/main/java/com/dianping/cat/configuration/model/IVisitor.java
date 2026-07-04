package com.dianping.cat.configuration.model;

import com.dianping.cat.configuration.model.entity.ClientConfig;
import com.dianping.cat.configuration.model.entity.Domain;
import com.dianping.cat.configuration.model.entity.Host;
import com.dianping.cat.configuration.model.entity.Property;
import com.dianping.cat.configuration.model.entity.Server;

public interface IVisitor {

   void visitConfig(ClientConfig config);

   void visitDomain(Domain domain);

   void visitHost(Host host);

   void visitProperty(Property property);

   void visitServer(Server server);
}
