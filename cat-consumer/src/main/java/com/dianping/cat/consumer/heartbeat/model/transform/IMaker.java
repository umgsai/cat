package com.dianping.cat.consumer.heartbeat.model.transform;

import com.dianping.cat.consumer.heartbeat.model.entity.Detail;
import com.dianping.cat.consumer.heartbeat.model.entity.Disk;
import com.dianping.cat.consumer.heartbeat.model.entity.Extension;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;

public interface IMaker<T> {

   Detail buildDetail(T node);

   Disk buildDisk(T node);

   String buildDomain(T node);

   Extension buildExtension(T node);

   HeartbeatReport buildHeartbeatReport(T node);

   String buildIp(T node);

   Machine buildMachine(T node);

   Period buildPeriod(T node);
}
