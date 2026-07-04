package com.dianping.cat.configuration.message;

import com.dianping.cat.configuration.message.entity.AtomicMessageConfig;
import com.dianping.cat.configuration.message.entity.Domain;
import com.dianping.cat.configuration.message.entity.Property;

public interface IVisitor {

   void visitAtomicMessageConfig(AtomicMessageConfig atomicMessageConfig);

   void visitDomain(Domain domain);

   void visitProperty(Property property);
}
