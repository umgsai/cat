package com.dianping.cat.consumer.state.model.transform;

import com.dianping.cat.consumer.state.model.entity.Detail;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;

public interface IMaker<T> {

   Detail buildDetail(T node);

   String buildIp(T node);

   Machine buildMachine(T node);

   Message buildMessage(T node);

   ProcessDomain buildProcessDomain(T node);

   StateReport buildStateReport(T node);
}
