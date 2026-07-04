package com.dianping.cat.configuration.message.transform;

import com.dianping.cat.configuration.message.entity.AtomicMessageConfig;
import com.dianping.cat.configuration.message.entity.Domain;
import com.dianping.cat.configuration.message.entity.Property;

public interface IMaker<T> {

   AtomicMessageConfig buildAtomicMessageConfig(T node);

   Domain buildDomain(T node);

   Property buildProperty(T node);
}
