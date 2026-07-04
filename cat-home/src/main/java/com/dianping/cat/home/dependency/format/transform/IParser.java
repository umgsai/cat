package com.dianping.cat.home.dependency.format.transform;

import com.dianping.cat.home.dependency.format.entity.Domain;
import com.dianping.cat.home.dependency.format.entity.ProductLine;
import com.dianping.cat.home.dependency.format.entity.TopoGraphFormatConfig;

public interface IParser<T> {
   TopoGraphFormatConfig parse(IMaker<T> maker, ILinker linker, T node);

   void parseForDomain(IMaker<T> maker, ILinker linker, Domain parent, T node);

   void parseForProductLine(IMaker<T> maker, ILinker linker, ProductLine parent, T node);
}
