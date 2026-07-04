package com.dianping.cat.home.resource.transform;

import com.dianping.cat.home.resource.entity.Resource;
import com.dianping.cat.home.resource.entity.ResourceConfig;

public interface IMaker<T> {

   Resource buildResource(T node);

   ResourceConfig buildResourceConfig(T node);
}
