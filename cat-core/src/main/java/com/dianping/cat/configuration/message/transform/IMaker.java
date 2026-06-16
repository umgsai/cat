package com.dianping.cat.configuration.message.transform;

import com.dianping.cat.configuration.message.entity.AtomicMessageConfig;
import com.dianping.cat.configuration.message.entity.Domain;
import com.dianping.cat.configuration.message.entity.Property;

public interface IMaker<T> {

   public AtomicMessageConfig buildAtomicMessageConfig(T node);

   public Domain buildDomain(T node);

   public Property buildProperty(T node);
}
