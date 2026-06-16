package com.dianping.cat.configuration.tp.transform;

import com.dianping.cat.configuration.tp.entity.Domain;
import com.dianping.cat.configuration.tp.entity.TpValueStatisticConfig;

public interface IMaker<T> {

   public Domain buildDomain(T node);

   public TpValueStatisticConfig buildTpValueStatisticConfig(T node);

   public String buildTransactionType(T node);
}
