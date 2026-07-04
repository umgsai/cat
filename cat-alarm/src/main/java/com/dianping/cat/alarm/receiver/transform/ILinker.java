package com.dianping.cat.alarm.receiver.transform;

import com.dianping.cat.alarm.receiver.entity.AlertConfig;
import com.dianping.cat.alarm.receiver.entity.Receiver;

public interface ILinker {

   boolean onReceiver(AlertConfig parent, Receiver receiver);
}
