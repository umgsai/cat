package com.dianping.cat.home.resource.transform;

import com.dianping.cat.home.resource.entity.Resource;
import com.dianping.cat.home.resource.entity.ResourceConfig;

public interface IParser<T> {
   ResourceConfig parse(IMaker<T> maker, ILinker linker, T node);

   void parseForResource(IMaker<T> maker, ILinker linker, Resource parent, T node);
}
