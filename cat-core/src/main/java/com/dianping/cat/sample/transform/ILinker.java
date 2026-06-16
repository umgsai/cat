package com.dianping.cat.sample.transform;

import com.dianping.cat.sample.entity.Domain;
import com.dianping.cat.sample.entity.SampleConfig;

public interface ILinker {

   public boolean onDomain(SampleConfig parent, Domain domain);
}
