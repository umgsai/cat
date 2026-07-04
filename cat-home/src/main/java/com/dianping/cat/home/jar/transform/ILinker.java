package com.dianping.cat.home.jar.transform;

import com.dianping.cat.home.jar.entity.Domain;
import com.dianping.cat.home.jar.entity.Jar;
import com.dianping.cat.home.jar.entity.JarReport;
import com.dianping.cat.home.jar.entity.Machine;

public interface ILinker {

   boolean onDomain(JarReport parent, Domain domain);

   boolean onJar(Machine parent, Jar jar);

   boolean onMachine(Domain parent, Machine machine);
}
