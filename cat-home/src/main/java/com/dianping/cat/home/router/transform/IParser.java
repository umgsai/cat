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

public interface IParser<T> {
   RouterConfig parse(IMaker<T> maker, ILinker linker, T node);

   void parseForDefaultServer(IMaker<T> maker, ILinker linker, DefaultServer parent, T node);

   void parseForDomain(IMaker<T> maker, ILinker linker, Domain parent, T node);

   void parseForGroup(IMaker<T> maker, ILinker linker, Group parent, T node);

   void parseForGroupServer(IMaker<T> maker, ILinker linker, GroupServer parent, T node);

   void parseForNetwork(IMaker<T> maker, ILinker linker, Network parent, T node);

   void parseForNetworkPolicy(IMaker<T> maker, ILinker linker, NetworkPolicy parent, T node);

   void parseForServer(IMaker<T> maker, ILinker linker, Server parent, T node);

   void parseForServerGroup(IMaker<T> maker, ILinker linker, ServerGroup parent, T node);
}
