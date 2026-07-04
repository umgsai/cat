package com.dianping.cat.home.service.transform;

import com.dianping.cat.home.service.entity.Domain;
import com.dianping.cat.home.service.entity.ServiceReport;

public interface IMaker<T> {

   Domain buildDomain(T node);

   ServiceReport buildServiceReport(T node);
}
