package com.dianping.cat.home.group.transform;

import com.dianping.cat.home.group.entity.Domain;
import com.dianping.cat.home.group.entity.DomainGroup;
import com.dianping.cat.home.group.entity.Group;

public interface IMaker<T> {

   Domain buildDomain(T node);

   DomainGroup buildDomainGroup(T node);

   Group buildGroup(T node);

   String buildIp(T node);
}
