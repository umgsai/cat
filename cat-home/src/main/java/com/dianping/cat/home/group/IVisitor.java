package com.dianping.cat.home.group;

import com.dianping.cat.home.group.entity.Domain;
import com.dianping.cat.home.group.entity.DomainGroup;
import com.dianping.cat.home.group.entity.Group;

public interface IVisitor {

   void visitDomain(Domain domain);

   void visitDomainGroup(DomainGroup domainGroup);

   void visitGroup(Group group);
}
