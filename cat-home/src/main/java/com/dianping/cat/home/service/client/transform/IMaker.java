package com.dianping.cat.home.service.client.transform;

import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.entity.Domain;
import com.dianping.cat.home.service.client.entity.Method;

public interface IMaker<T> {

   ClientReport buildClientReport(T node);

   Domain buildDomain(T node);

   Method buildMethod(T node);
}
