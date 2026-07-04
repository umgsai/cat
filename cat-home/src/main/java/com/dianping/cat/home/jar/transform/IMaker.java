package com.dianping.cat.home.jar.transform;

import com.dianping.cat.home.jar.entity.Domain;
import com.dianping.cat.home.jar.entity.Jar;
import com.dianping.cat.home.jar.entity.JarReport;
import com.dianping.cat.home.jar.entity.Machine;

public interface IMaker<T> {

   Domain buildDomain(T node);

   Jar buildJar(T node);

   JarReport buildJarReport(T node);

   Machine buildMachine(T node);
}
