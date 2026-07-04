package com.dianping.cat.consumer.state.model;

import com.dianping.cat.consumer.state.model.entity.Detail;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;

public interface IVisitor {

   void visitDetail(Detail detail);

   void visitMachine(Machine machine);

   void visitMessage(Message message);

   void visitProcessDomain(ProcessDomain processDomain);

   void visitStateReport(StateReport stateReport);
}
