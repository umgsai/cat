package com.dianping.cat.home.service.client.transform;

import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.entity.Domain;
import com.dianping.cat.home.service.client.entity.Method;

public interface ILinker {

   public boolean onDomain(ClientReport parent, Domain domain);

   public boolean onMethod(Domain parent, Method method);
}
