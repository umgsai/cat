package com.dianping.cat.configuration.tp.transform;

import com.dianping.cat.configuration.tp.entity.Domain;
import com.dianping.cat.configuration.tp.entity.TpValueStatisticConfig;

public interface IMaker<T> {

   Domain buildDomain(T node);

   TpValueStatisticConfig buildTpValueStatisticConfig(T node);

   String buildTransactionType(T node);
}
