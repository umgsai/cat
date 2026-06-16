package com.dianping.cat.home.router.transform;

import com.dianping.cat.home.router.entity.DefaultServer;
import com.dianping.cat.home.router.entity.Domain;
import com.dianping.cat.home.router.entity.Group;
import com.dianping.cat.home.router.entity.GroupServer;
import com.dianping.cat.home.router.entity.Network;
import com.dianping.cat.home.router.entity.NetworkPolicy;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.entity.Server;
import com.dianping.cat.home.router.entity.ServerGroup;

public interface ILinker {

   public boolean onDefaultServer(RouterConfig parent, DefaultServer defaultServer);

   public boolean onDomain(RouterConfig parent, Domain domain);

   public boolean onGroup(Domain parent, Group group);

   public boolean onGroupServer(ServerGroup parent, GroupServer groupServer);

   public boolean onNetwork(NetworkPolicy parent, Network network);

   public boolean onNetworkPolicy(RouterConfig parent, NetworkPolicy networkPolicy);

   public boolean onServer(Group parent, Server server);

   public boolean onServerGroup(RouterConfig parent, ServerGroup serverGroup);
}
