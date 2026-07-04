package com.dianping.cat.home.utilization.transform;

import com.dianping.cat.home.utilization.entity.ApplicationState;
import com.dianping.cat.home.utilization.entity.Domain;
import com.dianping.cat.home.utilization.entity.MachineState;
import com.dianping.cat.home.utilization.entity.UtilizationReport;

public interface ILinker {

   boolean onApplicationState(Domain parent, ApplicationState applicationState);

   boolean onDomain(UtilizationReport parent, Domain domain);

   boolean onMachineState(Domain parent, MachineState machineState);
}
