package com.dianping.cat.sample.transform;

import com.dianping.cat.sample.entity.Domain;
import com.dianping.cat.sample.entity.SampleConfig;

public interface IMaker<T> {

   public Domain buildDomain(T node);

   public SampleConfig buildSampleConfig(T node);
}
