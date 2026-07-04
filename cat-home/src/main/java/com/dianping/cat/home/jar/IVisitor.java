package com.dianping.cat.home.jar;

import com.dianping.cat.home.jar.entity.Domain;
import com.dianping.cat.home.jar.entity.Jar;
import com.dianping.cat.home.jar.entity.JarReport;
import com.dianping.cat.home.jar.entity.Machine;

public interface IVisitor {

   void visitDomain(Domain domain);

   void visitJar(Jar jar);

   void visitJarReport(JarReport jarReport);

   void visitMachine(Machine machine);
}
