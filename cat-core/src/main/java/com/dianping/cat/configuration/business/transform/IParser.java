package com.dianping.cat.configuration.business.transform;

import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;

public interface IParser<T> {
   public BusinessReportConfig parse(IMaker<T> maker, ILinker linker, T node);

   public void parseForBusinessItemConfig(IMaker<T> maker, ILinker linker, BusinessItemConfig parent, T node);

   public void parseForCustomConfig(IMaker<T> maker, ILinker linker, CustomConfig parent, T node);
}
