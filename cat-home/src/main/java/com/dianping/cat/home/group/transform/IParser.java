package com.dianping.cat.home.group.transform;

import com.dianping.cat.home.group.entity.Domain;
import com.dianping.cat.home.group.entity.DomainGroup;
import com.dianping.cat.home.group.entity.Group;

public interface IParser<T> {
   DomainGroup parse(IMaker<T> maker, ILinker linker, T node);

   void parseForDomain(IMaker<T> maker, ILinker linker, Domain parent, T node);

   void parseForGroup(IMaker<T> maker, ILinker linker, Group parent, T node);
}
