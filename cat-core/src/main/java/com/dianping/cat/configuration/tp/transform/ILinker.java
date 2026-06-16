package com.dianping.cat.configuration.tp.transform;

import com.dianping.cat.configuration.tp.entity.Domain;
import com.dianping.cat.configuration.tp.entity.TpValueStatisticConfig;

public interface ILinker {

   public boolean onDomain(TpValueStatisticConfig parent, Domain domain);
}
