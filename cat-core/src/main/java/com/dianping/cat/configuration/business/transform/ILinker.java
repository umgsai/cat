package com.dianping.cat.configuration.business.transform;

import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;

public interface ILinker {

   public boolean onBusinessItemConfig(BusinessReportConfig parent, BusinessItemConfig businessItemConfig);

   public boolean onCustomConfig(BusinessReportConfig parent, CustomConfig customConfig);
}
