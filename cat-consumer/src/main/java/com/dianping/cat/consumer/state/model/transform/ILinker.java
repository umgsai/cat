package com.dianping.cat.consumer.state.model.transform;

import com.dianping.cat.consumer.state.model.entity.Detail;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;

public interface ILinker {

   boolean onDetail(ProcessDomain parent, Detail detail);

   boolean onMachine(StateReport parent, Machine machine);

   boolean onMessage(Machine parent, Message message);

   boolean onProcessDomain(Machine parent, ProcessDomain processDomain);
}
