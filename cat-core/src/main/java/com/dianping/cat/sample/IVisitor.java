package com.dianping.cat.sample;

import com.dianping.cat.sample.entity.Domain;
import com.dianping.cat.sample.entity.SampleConfig;

public interface IVisitor {

   public void visitDomain(Domain domain);

   public void visitSampleConfig(SampleConfig sampleConfig);
}
