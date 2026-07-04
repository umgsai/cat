package com.dianping.cat.alarm.receiver;

import com.dianping.cat.alarm.receiver.entity.AlertConfig;
import com.dianping.cat.alarm.receiver.entity.Receiver;

public interface IVisitor {

   void visitAlertConfig(AlertConfig alertConfig);

   void visitReceiver(Receiver receiver);
}
