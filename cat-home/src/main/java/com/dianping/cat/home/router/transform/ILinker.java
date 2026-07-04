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

   boolean onDefaultServer(RouterConfig parent, DefaultServer defaultServer);

   boolean onDomain(RouterConfig parent, Domain domain);

   boolean onGroup(Domain parent, Group group);

   boolean onGroupServer(ServerGroup parent, GroupServer groupServer);

   boolean onNetwork(NetworkPolicy parent, Network network);

   boolean onNetworkPolicy(RouterConfig parent, NetworkPolicy networkPolicy);

   boolean onServer(Group parent, Server server);

   boolean onServerGroup(RouterConfig parent, ServerGroup serverGroup);
}
