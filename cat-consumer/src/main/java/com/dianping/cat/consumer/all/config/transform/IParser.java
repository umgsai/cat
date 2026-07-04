package com.dianping.cat.consumer.all.config.transform;

import com.dianping.cat.consumer.all.config.entity.AllConfig;
import com.dianping.cat.consumer.all.config.entity.Name;
import com.dianping.cat.consumer.all.config.entity.Report;
import com.dianping.cat.consumer.all.config.entity.Type;

public interface IParser<T> {
   AllConfig parse(IMaker<T> maker, ILinker linker, T node);

   void parseForName(IMaker<T> maker, ILinker linker, Name parent, T node);

   void parseForReport(IMaker<T> maker, ILinker linker, Report parent, T node);

   void parseForType(IMaker<T> maker, ILinker linker, Type parent, T node);
}
