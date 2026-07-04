package com.dianping.cat.sample.transform;

import com.dianping.cat.sample.entity.Domain;
import com.dianping.cat.sample.entity.SampleConfig;

public interface IMaker<T> {

   Domain buildDomain(T node);

   SampleConfig buildSampleConfig(T node);
}
