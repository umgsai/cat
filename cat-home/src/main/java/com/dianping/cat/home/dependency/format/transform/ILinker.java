package com.dianping.cat.home.dependency.format.transform;

import com.dianping.cat.home.dependency.format.entity.Domain;
import com.dianping.cat.home.dependency.format.entity.ProductLine;
import com.dianping.cat.home.dependency.format.entity.TopoGraphFormatConfig;

public interface ILinker {

   public boolean onDomain(ProductLine parent, Domain domain);

   public boolean onProductLine(TopoGraphFormatConfig parent, ProductLine productLine);
}
