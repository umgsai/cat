package com.dianping.cat.consumer.dependency.model;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;

public interface IVisitor {

   void visitDependency(Dependency dependency);

   void visitDependencyReport(DependencyReport dependencyReport);

   void visitIndex(Index index);

   void visitSegment(Segment segment);
}
