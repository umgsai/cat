package com.dianping.cat.home.resource;

import com.dianping.cat.home.resource.entity.Resource;
import com.dianping.cat.home.resource.entity.ResourceConfig;

public interface IVisitor {

   public void visitResource(Resource resource);

   public void visitResourceConfig(ResourceConfig resourceConfig);
}
