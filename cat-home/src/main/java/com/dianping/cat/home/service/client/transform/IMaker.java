package com.dianping.cat.home.service.client.transform;

import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.entity.Domain;
import com.dianping.cat.home.service.client.entity.Method;

public interface IMaker<T> {

   public ClientReport buildClientReport(T node);

   public Domain buildDomain(T node);

   public Method buildMethod(T node);
}
