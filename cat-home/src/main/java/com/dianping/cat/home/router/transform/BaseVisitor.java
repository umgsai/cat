package com.dianping.cat.home.router.transform;

import com.dianping.cat.home.router.IVisitor;
import com.dianping.cat.home.router.entity.DefaultServer;
import com.dianping.cat.home.router.entity.Domain;
import com.dianping.cat.home.router.entity.Group;
import com.dianping.cat.home.router.entity.GroupServer;
import com.dianping.cat.home.router.entity.Network;
import com.dianping.cat.home.router.entity.NetworkPolicy;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.entity.Server;
import com.dianping.cat.home.router.entity.ServerGroup;

public abstract class BaseVisitor implements IVisitor {
   @Override
   public void visitDefaultServer(DefaultServer defaultServer) {
   }

   @Override
   public void visitDomain(Domain domain) {
      for (Group group : domain.getGroups().values()) {
         visitGroup(group);
      }
   }

   @Override
   public void visitGroup(Group group) {
      for (Server server : group.getServers()) {
         visitServer(server);
      }
   }

   @Override
   public void visitGroupServer(GroupServer groupServer) {
   }

   @Override
   public void visitNetwork(Network network) {
   }

   @Override
   public void visitNetworkPolicy(NetworkPolicy networkPolicy) {
      for (Network network : networkPolicy.getNetworks().values()) {
         visitNetwork(network);
      }
   }

   @Override
   public void visitRouterConfig(RouterConfig routerConfig) {
      for (DefaultServer defaultServer : routerConfig.getDefaultServers().values()) {
         visitDefaultServer(defaultServer);
      }

      for (NetworkPolicy networkPolicy : routerConfig.getNetworkPolicies().values()) {
         visitNetworkPolicy(networkPolicy);
      }

      for (ServerGroup serverGroup : routerConfig.getServerGroups().values()) {
         visitServerGroup(serverGroup);
      }

      for (Domain domain : routerConfig.getDomains().values()) {
         visitDomain(domain);
      }
   }

   @Override
   public void visitServer(Server server) {
   }

   @Override
   public void visitServerGroup(ServerGroup serverGroup) {
      for (GroupServer groupServer : serverGroup.getGroupServers().values()) {
         visitGroupServer(groupServer);
      }
   }
}
