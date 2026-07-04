package com.dianping.cat.home.dependency.format.transform;

import com.dianping.cat.home.dependency.format.entity.Domain;
import com.dianping.cat.home.dependency.format.entity.ProductLine;
import com.dianping.cat.home.dependency.format.entity.TopoGraphFormatConfig;

public interface IMaker<T> {

   Domain buildDomain(T node);

   ProductLine buildProductLine(T node);

   TopoGraphFormatConfig buildTopoGraphFormatConfig(T node);
}
