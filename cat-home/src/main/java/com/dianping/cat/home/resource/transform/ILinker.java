package com.dianping.cat.home.resource.transform;

import com.dianping.cat.home.resource.entity.Resource;
import com.dianping.cat.home.resource.entity.ResourceConfig;

public interface ILinker {

   public boolean onResource(ResourceConfig parent, Resource resource);
}
