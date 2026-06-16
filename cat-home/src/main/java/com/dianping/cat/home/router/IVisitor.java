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

   public void visitDefaultServer(DefaultServer defaultServer);

   public void visitDomain(Domain domain);

   public void visitGroup(Group group);

   public void visitGroupServer(GroupServer groupServer);

   public void visitNetwork(Network network);

   public void visitNetworkPolicy(NetworkPolicy networkPolicy);

   public void visitRouterConfig(RouterConfig routerConfig);

   public void visitServer(Server server);

   public void visitServerGroup(ServerGroup serverGroup);
}
