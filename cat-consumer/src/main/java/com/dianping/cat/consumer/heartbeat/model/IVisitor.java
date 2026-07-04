package com.dianping.cat.consumer.heartbeat.model;

import com.dianping.cat.consumer.heartbeat.model.entity.Detail;
import com.dianping.cat.consumer.heartbeat.model.entity.Disk;
import com.dianping.cat.consumer.heartbeat.model.entity.Extension;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;

public interface IVisitor {

   void visitDetail(Detail detail);

   void visitDisk(Disk disk);

   void visitExtension(Extension extension);

   void visitHeartbeatReport(HeartbeatReport heartbeatReport);

   void visitMachine(Machine machine);

   void visitPeriod(Period period);
}
