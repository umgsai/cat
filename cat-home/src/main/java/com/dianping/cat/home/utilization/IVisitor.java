package com.dianping.cat.home.utilization;

import com.dianping.cat.home.utilization.entity.ApplicationState;
import com.dianping.cat.home.utilization.entity.Domain;
import com.dianping.cat.home.utilization.entity.MachineState;
import com.dianping.cat.home.utilization.entity.UtilizationReport;

public interface IVisitor {

   void visitApplicationState(ApplicationState applicationState);

   void visitDomain(Domain domain);

   void visitMachineState(MachineState machineState);

   void visitUtilizationReport(UtilizationReport utilizationReport);
}
