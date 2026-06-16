package com.dianping.cat.home.resource.transform;

import com.dianping.cat.home.resource.entity.Resource;
import com.dianping.cat.home.resource.entity.ResourceConfig;

public interface IParser<T> {
   public ResourceConfig parse(IMaker<T> maker, ILinker linker, T node);

   public void parseForResource(IMaker<T> maker, ILinker linker, Resource parent, T node);
}
