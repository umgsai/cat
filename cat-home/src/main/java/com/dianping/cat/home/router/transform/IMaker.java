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

   DefaultServer buildDefaultServer(T node);

   Domain buildDomain(T node);

   Group buildGroup(T node);

   GroupServer buildGroupServer(T node);

   Network buildNetwork(T node);

   NetworkPolicy buildNetworkPolicy(T node);

   RouterConfig buildRouterConfig(T node);

   Server buildServer(T node);

   ServerGroup buildServerGroup(T node);
}
