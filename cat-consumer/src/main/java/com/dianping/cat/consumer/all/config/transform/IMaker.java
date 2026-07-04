package com.dianping.cat.consumer.all.config.transform;

import com.dianping.cat.consumer.all.config.entity.AllConfig;
import com.dianping.cat.consumer.all.config.entity.Name;
import com.dianping.cat.consumer.all.config.entity.Report;
import com.dianping.cat.consumer.all.config.entity.Type;

public interface IMaker<T> {

   AllConfig buildAllConfig(T node);

   Name buildName(T node);

   Report buildReport(T node);

   Type buildType(T node);
}
