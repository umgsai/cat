package com.dianping.cat.configuration.message.transform;

import com.dianping.cat.configuration.message.entity.AtomicMessageConfig;
import com.dianping.cat.configuration.message.entity.Domain;
import com.dianping.cat.configuration.message.entity.Property;

public interface IParser<T> {
   AtomicMessageConfig parse(IMaker<T> maker, ILinker linker, T node);

   void parseForDomain(IMaker<T> maker, ILinker linker, Domain parent, T node);

   void parseForProperty(IMaker<T> maker, ILinker linker, Property parent, T node);
}
