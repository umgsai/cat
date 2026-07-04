package com.dianping.cat.consumer.heartbeat.model.transform;

import com.dianping.cat.consumer.heartbeat.model.entity.Detail;
import com.dianping.cat.consumer.heartbeat.model.entity.Disk;
import com.dianping.cat.consumer.heartbeat.model.entity.Extension;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;

public interface ILinker {

   boolean onDetail(Extension parent, Detail detail);

   boolean onDisk(Period parent, Disk disk);

   boolean onExtension(Period parent, Extension extension);

   boolean onMachine(HeartbeatReport parent, Machine machine);

   boolean onPeriod(Machine parent, Period period);
}
