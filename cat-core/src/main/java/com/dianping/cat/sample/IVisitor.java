package com.dianping.cat.sample;

import com.dianping.cat.sample.entity.Domain;
import com.dianping.cat.sample.entity.SampleConfig;

public interface IVisitor {

   void visitDomain(Domain domain);

   void visitSampleConfig(SampleConfig sampleConfig);
}
