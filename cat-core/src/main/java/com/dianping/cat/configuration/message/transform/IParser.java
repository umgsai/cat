package com.dianping.cat.configuration.message.transform;

import com.dianping.cat.configuration.message.entity.AtomicMessageConfig;
import com.dianping.cat.configuration.message.entity.Domain;
import com.dianping.cat.configuration.message.entity.Property;

public interface IParser<T> {
   public AtomicMessageConfig parse(IMaker<T> maker, ILinker linker, T node);

   public void parseForDomain(IMaker<T> maker, ILinker linker, Domain parent, T node);

   public void parseForProperty(IMaker<T> maker, ILinker linker, Property parent, T node);
}
