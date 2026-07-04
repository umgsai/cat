package com.dianping.cat.configuration.message.transform;

import com.dianping.cat.configuration.message.entity.AtomicMessageConfig;
import com.dianping.cat.configuration.message.entity.Domain;
import com.dianping.cat.configuration.message.entity.Property;

public interface ILinker {

   boolean onDomain(AtomicMessageConfig parent, Domain domain);

   boolean onProperty(Domain parent, Property property);
}
