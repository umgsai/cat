package com.dianping.cat.home.resource;

import com.dianping.cat.home.resource.entity.Resource;
import com.dianping.cat.home.resource.entity.ResourceConfig;

public interface IVisitor {

   void visitResource(Resource resource);

   void visitResourceConfig(ResourceConfig resourceConfig);
}
