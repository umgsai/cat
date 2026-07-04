package com.dianping.cat.alarm.sender.transform;

import com.dianping.cat.alarm.sender.entity.Par;
import com.dianping.cat.alarm.sender.entity.Sender;
import com.dianping.cat.alarm.sender.entity.SenderConfig;

public interface IParser<T> {
   SenderConfig parse(IMaker<T> maker, ILinker linker, T node);

   void parseForPar(IMaker<T> maker, ILinker linker, Par parent, T node);

   void parseForSender(IMaker<T> maker, ILinker linker, Sender parent, T node);
}
