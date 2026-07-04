package com.dianping.cat.alarm.sender.transform;

import com.dianping.cat.alarm.sender.entity.Par;
import com.dianping.cat.alarm.sender.entity.Sender;
import com.dianping.cat.alarm.sender.entity.SenderConfig;

public interface IMaker<T> {

   Par buildPar(T node);

   Sender buildSender(T node);

   SenderConfig buildSenderConfig(T node);
}
