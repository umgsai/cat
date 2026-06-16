package com.dianping.cat.home.router.transform;

import java.util.Stack;

import com.dianping.cat.home.router.IEntity;
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

public class DefaultMerger implements IVisitor {

   private Stack<Object> m_objs = new Stack<Object>();

   private RouterConfig m_routerConfig;

   public DefaultMerger() {
   }

   public DefaultMerger(RouterConfig routerConfig) {
      m_routerConfig = routerConfig;
      m_objs.push(routerConfig);
   }

   public RouterConfig getRouterConfig() {
      return m_routerConfig;
   }

   protected Stack<Object> getObjects() {
      return m_objs;
   }

   public <T> void merge(IEntity<T> to, IEntity<T> from) {
      m_objs.push(to);
      from.accept(this);
      m_objs.pop();
   }

   protected void mergeDefaultServer(DefaultServer to, DefaultServer from) {
      to.mergeAttributes(from);
   }

   protected void mergeDomain(Domain to, Domain from) {
      to.mergeAttributes(from);
   }

   protected void mergeGroup(Group to, Group from) {
      to.mergeAttributes(from);
   }

   protected void mergeGroupServer(GroupServer to, GroupServer from) {
      to.mergeAttributes(from);
   }

   protected void mergeNetwork(Network to, Network from) {
      to.mergeAttributes(from);
   }

   protected void mergeNetworkPolicy(NetworkPolicy to, NetworkPolicy from) {
      to.mergeAttributes(from);
   }

   protected void mergeRouterConfig(RouterConfig to, RouterConfig from) {
      to.mergeAttributes(from);
   }

   protected void mergeServer(Server to, Server from) {
      to.mergeAttributes(from);
   }

   protected void mergeServerGroup(ServerGroup to, ServerGroup from) {
      to.mergeAttributes(from);
   }

   @Override
   public void visitDefaultServer(DefaultServer from) {
      DefaultServer to = (DefaultServer) m_objs.peek();

      mergeDefaultServer(to, from);
      visitDefaultServerChildren(to, from);
   }

   protected void visitDefaultServerChildren(DefaultServer to, DefaultServer from) {
   }

   @Override
   public void visitDomain(Domain from) {
      Domain to = (Domain) m_objs.peek();

      mergeDomain(to, from);
      visitDomainChildren(to, from);
   }

   protected void visitDomainChildren(Domain to, Domain from) {
      for (Group source : from.getGroups().values()) {
         Group target = to.findGroup(source.getId());

         if (target == null) {
            target = new Group(source.getId());
            to.addGroup(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitGroup(Group from) {
      Group to = (Group) m_objs.peek();

      mergeGroup(to, from);
      visitGroupChildren(to, from);
   }

   protected void visitGroupChildren(Group to, Group from) {
      for (Server source : from.getServers()) {
         Server target = null;

         if (target == null) {
            target = new Server();
            to.addServer(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitGroupServer(GroupServer from) {
      GroupServer to = (GroupServer) m_objs.peek();

      mergeGroupServer(to, from);
      visitGroupServerChildren(to, from);
   }

   protected void visitGroupServerChildren(GroupServer to, GroupServer from) {
   }

   @Override
   public void visitNetwork(Network from) {
      Network to = (Network) m_objs.peek();

      mergeNetwork(to, from);
      visitNetworkChildren(to, from);
   }

   protected void visitNetworkChildren(Network to, Network from) {
   }

   @Override
   public void visitNetworkPolicy(NetworkPolicy from) {
      NetworkPolicy to = (NetworkPolicy) m_objs.peek();

      mergeNetworkPolicy(to, from);
      visitNetworkPolicyChildren(to, from);
   }

   protected void visitNetworkPolicyChildren(NetworkPolicy to, NetworkPolicy from) {
      for (Network source : from.getNetworks().values()) {
         Network target = to.findNetwork(source.getId());

         if (target == null) {
            target = new Network(source.getId());
            to.addNetwork(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitRouterConfig(RouterConfig from) {
      RouterConfig to = (RouterConfig) m_objs.peek();

      mergeRouterConfig(to, from);
      visitRouterConfigChildren(to, from);
   }

   protected void visitRouterConfigChildren(RouterConfig to, RouterConfig from) {
      for (DefaultServer source : from.getDefaultServers().values()) {
         DefaultServer target = to.findDefaultServer(source.getId());

         if (target == null) {
            target = new DefaultServer(source.getId());
            to.addDefaultServer(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }

      for (NetworkPolicy source : from.getNetworkPolicies().values()) {
         NetworkPolicy target = to.findNetworkPolicy(source.getId());

         if (target == null) {
            target = new NetworkPolicy(source.getId());
            to.addNetworkPolicy(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }

      for (ServerGroup source : from.getServerGroups().values()) {
         ServerGroup target = to.findServerGroup(source.getId());

         if (target == null) {
            target = new ServerGroup(source.getId());
            to.addServerGroup(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }

      for (Domain source : from.getDomains().values()) {
         Domain target = to.findDomain(source.getId());

         if (target == null) {
            target = new Domain(source.getId());
            to.addDomain(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitServer(Server from) {
      Server to = (Server) m_objs.peek();

      mergeServer(to, from);
      visitServerChildren(to, from);
   }

   protected void visitServerChildren(Server to, Server from) {
   }

   @Override
   public void visitServerGroup(ServerGroup from) {
      ServerGroup to = (ServerGroup) m_objs.peek();

      mergeServerGroup(to, from);
      visitServerGroupChildren(to, from);
   }

   protected void visitServerGroupChildren(ServerGroup to, ServerGroup from) {
      for (GroupServer source : from.getGroupServers().values()) {
         GroupServer target = to.findGroupServer(source.getId());

         if (target == null) {
            target = new GroupServer(source.getId());
            to.addGroupServer(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }
}
