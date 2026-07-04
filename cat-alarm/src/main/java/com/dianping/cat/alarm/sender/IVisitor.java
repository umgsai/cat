package com.dianping.cat.alarm.sender;

import com.dianping.cat.alarm.sender.entity.Par;
import com.dianping.cat.alarm.sender.entity.Sender;
import com.dianping.cat.alarm.sender.entity.SenderConfig;

public interface IVisitor {

   void visitPar(Par par);

   void visitSender(Sender sender);

   void visitSenderConfig(SenderConfig senderConfig);
}
