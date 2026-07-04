package com.dianping.cat.home.router;

import com.dianping.cat.home.router.entity.DefaultServer;
import com.dianping.cat.home.router.entity.Domain;
import com.dianping.cat.home.router.entity.Group;
import com.dianping.cat.home.router.entity.GroupServer;
import com.dianping.cat.home.router.entity.Network;
import com.dianping.cat.home.router.entity.NetworkPolicy;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.entity.Server;
import com.dianping.cat.home.router.entity.ServerGroup;

public interface IVisitor {

   void visitDefaultServer(DefaultServer defaultServer);

   void visitDomain(Domain domain);

   void visitGroup(Group group);

   void visitGroupServer(GroupServer groupServer);

   void visitNetwork(Network network);

   void visitNetworkPolicy(NetworkPolicy networkPolicy);

   void visitRouterConfig(RouterConfig routerConfig);

   void visitServer(Server server);

   void visitServerGroup(ServerGroup serverGroup);
}
