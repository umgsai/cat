package com.dianping.cat.home.service.client.transform;

import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.entity.Domain;
import com.dianping.cat.home.service.client.entity.Method;

public interface IParser<T> {
   public ClientReport parse(IMaker<T> maker, ILinker linker, T node);

   public void parseForDomain(IMaker<T> maker, ILinker linker, Domain parent, T node);

   public void parseForMethod(IMaker<T> maker, ILinker linker, Method parent, T node);
}
