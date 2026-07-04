package com.dianping.cat.home.dependency.format;

import com.dianping.cat.home.dependency.format.entity.Domain;
import com.dianping.cat.home.dependency.format.entity.ProductLine;
import com.dianping.cat.home.dependency.format.entity.TopoGraphFormatConfig;

public interface IVisitor {

   void visitDomain(Domain domain);

   void visitProductLine(ProductLine productLine);

   void visitTopoGraphFormatConfig(TopoGraphFormatConfig topoGraphFormatConfig);
}
