package com.dianping.cat.configuration.tp;

import com.dianping.cat.configuration.tp.entity.Domain;
import com.dianping.cat.configuration.tp.entity.TpValueStatisticConfig;

public interface IVisitor {

   public void visitDomain(Domain domain);

   public void visitTpValueStatisticConfig(TpValueStatisticConfig tpValueStatisticConfig);
}
