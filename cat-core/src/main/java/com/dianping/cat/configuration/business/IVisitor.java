package com.dianping.cat.configuration.business;

import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;

public interface IVisitor {

   public void visitBusinessItemConfig(BusinessItemConfig businessItemConfig);

   public void visitBusinessReportConfig(BusinessReportConfig businessReportConfig);

   public void visitCustomConfig(CustomConfig customConfig);
}
