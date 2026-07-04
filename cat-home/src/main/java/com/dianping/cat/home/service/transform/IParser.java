package com.dianping.cat.home.service.transform;

import com.dianping.cat.home.service.entity.Domain;
import com.dianping.cat.home.service.entity.ServiceReport;

public interface IParser<T> {
   ServiceReport parse(IMaker<T> maker, ILinker linker, T node);

   void parseForDomain(IMaker<T> maker, ILinker linker, Domain parent, T node);
}
