package com.dianping.cat.consumer.dependency.model.transform;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;

public interface IMaker<T> {

   Dependency buildDependency(T node);

   DependencyReport buildDependencyReport(T node);

   String buildDomainName(T node);

   Index buildIndex(T node);

   Segment buildSegment(T node);
}
