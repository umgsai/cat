package com.dianping.cat.home.utilization.transform;

import com.dianping.cat.home.utilization.entity.ApplicationState;
import com.dianping.cat.home.utilization.entity.Domain;
import com.dianping.cat.home.utilization.entity.MachineState;
import com.dianping.cat.home.utilization.entity.UtilizationReport;

public interface IMaker<T> {

   ApplicationState buildApplicationState(T node);

   Domain buildDomain(T node);

   MachineState buildMachineState(T node);

   UtilizationReport buildUtilizationReport(T node);
}
