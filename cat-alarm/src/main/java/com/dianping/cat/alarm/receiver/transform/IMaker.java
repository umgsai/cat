package com.dianping.cat.alarm.receiver.transform;

import com.dianping.cat.alarm.receiver.entity.AlertConfig;
import com.dianping.cat.alarm.receiver.entity.Receiver;

public interface IMaker<T> {

   AlertConfig buildAlertConfig(T node);

   String buildDx(T node);

   String buildEmail(T node);

   String buildPhone(T node);

   Receiver buildReceiver(T node);

   String buildWeixin(T node);
}
