package com.dianping.cat.home.utilization.transform;

import com.dianping.cat.home.utilization.entity.ApplicationState;
import com.dianping.cat.home.utilization.entity.Domain;
import com.dianping.cat.home.utilization.entity.MachineState;
import com.dianping.cat.home.utilization.entity.UtilizationReport;

public interface IParser<T> {
   UtilizationReport parse(IMaker<T> maker, ILinker linker, T node);

   void parseForApplicationState(IMaker<T> maker, ILinker linker, ApplicationState parent, T node);

   void parseForDomain(IMaker<T> maker, ILinker linker, Domain parent, T node);

   void parseForMachineState(IMaker<T> maker, ILinker linker, MachineState parent, T node);
}
