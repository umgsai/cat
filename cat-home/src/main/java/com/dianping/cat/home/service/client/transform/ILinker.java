package com.dianping.cat.home.service.client.transform;

import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.entity.Domain;
import com.dianping.cat.home.service.client.entity.Method;

public interface ILinker {

   boolean onDomain(ClientReport parent, Domain domain);

   boolean onMethod(Domain parent, Method method);
}
