package com.dianping.cat.consumer.cross.model.transform;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.entity.Local;
import com.dianping.cat.consumer.cross.model.entity.Name;
import com.dianping.cat.consumer.cross.model.entity.Remote;
import com.dianping.cat.consumer.cross.model.entity.Type;

public interface ILinker {

   boolean onLocal(CrossReport parent, Local local);

   boolean onName(Type parent, Name name);

   boolean onRemote(Local parent, Remote remote);

   boolean onType(Remote parent, Type type);
}
