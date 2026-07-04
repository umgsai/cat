package com.dianping.cat.consumer.cross.model.transform;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.entity.Local;
import com.dianping.cat.consumer.cross.model.entity.Name;
import com.dianping.cat.consumer.cross.model.entity.Remote;
import com.dianping.cat.consumer.cross.model.entity.Type;

public interface IParser<T> {
   CrossReport parse(IMaker<T> maker, ILinker linker, T node);

   void parseForLocal(IMaker<T> maker, ILinker linker, Local parent, T node);

   void parseForName(IMaker<T> maker, ILinker linker, Name parent, T node);

   void parseForRemote(IMaker<T> maker, ILinker linker, Remote parent, T node);

   void parseForType(IMaker<T> maker, ILinker linker, Type parent, T node);
}
