package com.dianping.cat.home.resource.transform;

import com.dianping.cat.home.resource.entity.Resource;
import com.dianping.cat.home.resource.entity.ResourceConfig;

public interface IMaker<T> {

   public Resource buildResource(T node);

   public ResourceConfig buildResourceConfig(T node);
}
