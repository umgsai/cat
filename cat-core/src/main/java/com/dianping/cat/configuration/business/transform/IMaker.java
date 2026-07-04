package com.dianping.cat.configuration.business.transform;

import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;

public interface IMaker<T> {

   BusinessItemConfig buildBusinessItemConfig(T node);

   BusinessReportConfig buildBusinessReportConfig(T node);

   CustomConfig buildCustomConfig(T node);
}
