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

public interface IMaker<T> {

   public DefaultServer buildDefaultServer(T node);

   public Domain buildDomain(T node);

   public Group buildGroup(T node);

   public GroupServer buildGroupServer(T node);

   public Network buildNetwork(T node);

   public NetworkPolicy buildNetworkPolicy(T node);

   public RouterConfig buildRouterConfig(T node);

   public Server buildServer(T node);

   public ServerGroup buildServerGroup(T node);
}
