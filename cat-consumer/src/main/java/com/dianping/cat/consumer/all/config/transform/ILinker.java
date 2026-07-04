package com.dianping.cat.consumer.all.config.transform;

import com.dianping.cat.consumer.all.config.entity.AllConfig;
import com.dianping.cat.consumer.all.config.entity.Name;
import com.dianping.cat.consumer.all.config.entity.Report;
import com.dianping.cat.consumer.all.config.entity.Type;

public interface ILinker {

   boolean onName(Type parent, Name name);

   boolean onReport(AllConfig parent, Report report);

   boolean onType(Report parent, Type type);
}
