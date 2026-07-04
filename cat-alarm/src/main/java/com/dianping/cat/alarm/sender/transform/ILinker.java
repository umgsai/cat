package com.dianping.cat.alarm.sender.transform;

import com.dianping.cat.alarm.sender.entity.Par;
import com.dianping.cat.alarm.sender.entity.Sender;
import com.dianping.cat.alarm.sender.entity.SenderConfig;

public interface ILinker {

   boolean onPar(Sender parent, Par par);

   boolean onSender(SenderConfig parent, Sender sender);
}
