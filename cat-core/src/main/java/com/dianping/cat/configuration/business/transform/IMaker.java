package com.dianping.cat.configuration.business.transform;

import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;

public interface IMaker<T> {

   public BusinessItemConfig buildBusinessItemConfig(T node);

   public BusinessReportConfig buildBusinessReportConfig(T node);

   public CustomConfig buildCustomConfig(T node);
}
