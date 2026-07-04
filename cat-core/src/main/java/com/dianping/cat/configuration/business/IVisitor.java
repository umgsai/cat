package com.dianping.cat.configuration.business;

import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;

public interface IVisitor {

   void visitBusinessItemConfig(BusinessItemConfig businessItemConfig);

   void visitBusinessReportConfig(BusinessReportConfig businessReportConfig);

   void visitCustomConfig(CustomConfig customConfig);
}
