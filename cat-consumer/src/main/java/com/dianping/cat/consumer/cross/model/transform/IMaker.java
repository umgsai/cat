package com.dianping.cat.consumer.cross.model.transform;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.entity.Local;
import com.dianping.cat.consumer.cross.model.entity.Name;
import com.dianping.cat.consumer.cross.model.entity.Remote;
import com.dianping.cat.consumer.cross.model.entity.Type;

public interface IMaker<T> {

   CrossReport buildCrossReport(T node);

   String buildDomain(T node);

   String buildIp(T node);

   Local buildLocal(T node);

   Name buildName(T node);

   Remote buildRemote(T node);

   Type buildType(T node);
}
