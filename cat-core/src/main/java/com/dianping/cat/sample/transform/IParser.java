package com.dianping.cat.sample.transform;

import com.dianping.cat.sample.entity.Domain;
import com.dianping.cat.sample.entity.SampleConfig;

public interface IParser<T> {
   SampleConfig parse(IMaker<T> maker, ILinker linker, T node);

   void parseForDomain(IMaker<T> maker, ILinker linker, Domain parent, T node);
}
