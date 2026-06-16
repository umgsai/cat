package com.dianping.cat.configuration.message.transform;

import com.dianping.cat.configuration.message.entity.AtomicMessageConfig;
import com.dianping.cat.configuration.message.entity.Domain;
import com.dianping.cat.configuration.message.entity.Property;

public interface ILinker {

   public boolean onDomain(AtomicMessageConfig parent, Domain domain);

   public boolean onProperty(Domain parent, Property property);
}
