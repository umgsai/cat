package com.dianping.cat.consumer.heartbeat.model.transform;

import com.dianping.cat.consumer.heartbeat.model.entity.Detail;
import com.dianping.cat.consumer.heartbeat.model.entity.Disk;
import com.dianping.cat.consumer.heartbeat.model.entity.Extension;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;

public interface IParser<T> {
   HeartbeatReport parse(IMaker<T> maker, ILinker linker, T node);

   void parseForDetail(IMaker<T> maker, ILinker linker, Detail parent, T node);

   void parseForDisk(IMaker<T> maker, ILinker linker, Disk parent, T node);

   void parseForExtension(IMaker<T> maker, ILinker linker, Extension parent, T node);

   void parseForMachine(IMaker<T> maker, ILinker linker, Machine parent, T node);

   void parseForPeriod(IMaker<T> maker, ILinker linker, Period parent, T node);
}
