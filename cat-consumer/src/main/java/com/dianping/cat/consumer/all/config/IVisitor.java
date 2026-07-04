package com.dianping.cat.consumer.all.config;

import com.dianping.cat.consumer.all.config.entity.AllConfig;
import com.dianping.cat.consumer.all.config.entity.Name;
import com.dianping.cat.consumer.all.config.entity.Report;
import com.dianping.cat.consumer.all.config.entity.Type;

public interface IVisitor {

   void visitAllConfig(AllConfig allConfig);

   void visitName(Name name);

   void visitReport(Report report);

   void visitType(Type type);
}
