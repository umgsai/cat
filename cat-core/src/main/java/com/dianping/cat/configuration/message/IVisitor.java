package com.dianping.cat.configuration.message;

import com.dianping.cat.configuration.message.entity.AtomicMessageConfig;
import com.dianping.cat.configuration.message.entity.Domain;
import com.dianping.cat.configuration.message.entity.Property;

public interface IVisitor {

   public void visitAtomicMessageConfig(AtomicMessageConfig atomicMessageConfig);

   public void visitDomain(Domain domain);

   public void visitProperty(Property property);
}
