package com.dianping.cat.home.dependency.format;

import com.dianping.cat.home.dependency.format.entity.Domain;
import com.dianping.cat.home.dependency.format.entity.ProductLine;
import com.dianping.cat.home.dependency.format.entity.TopoGraphFormatConfig;

public interface IVisitor {

   public void visitDomain(Domain domain);

   public void visitProductLine(ProductLine productLine);

   public void visitTopoGraphFormatConfig(TopoGraphFormatConfig topoGraphFormatConfig);
}
