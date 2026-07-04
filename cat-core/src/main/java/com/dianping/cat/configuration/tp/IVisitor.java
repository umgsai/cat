package com.dianping.cat.configuration.tp;

import com.dianping.cat.configuration.tp.entity.Domain;
import com.dianping.cat.configuration.tp.entity.TpValueStatisticConfig;

public interface IVisitor {

   void visitDomain(Domain domain);

   void visitTpValueStatisticConfig(TpValueStatisticConfig tpValueStatisticConfig);
}
