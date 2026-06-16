package com.dianping.cat.home.router.transform;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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

public class DefaultNativeBuilder implements IVisitor {

   private IVisitor m_visitor;

   private DataOutputStream m_out;

   public DefaultNativeBuilder(OutputStream out) {
      this(out, null);
   }

   public DefaultNativeBuilder(OutputStream out, IVisitor visitor) {
      m_out = new DataOutputStream(out);
      m_visitor = (visitor == null ? this : visitor);
   }

   public static byte[] build(RouterConfig routerConfig) {
      ByteArrayOutputStream out = new ByteArrayOutputStream(8192);

      build(routerConfig, out);
      return out.toByteArray();
   }

   public static void build(RouterConfig routerConfig, OutputStream out) {
      routerConfig.accept(new DefaultNativeBuilder(out));
   }

   @Override
   public void visitDefaultServer(DefaultServer defaultServer) {
      if (defaultServer.getId() != null) {
         writeTag(1, 1);
         writeString(defaultServer.getId());
      }

      writeTag(2, 0);
      writeDouble(defaultServer.getWeight());

      writeTag(3, 0);
      writeInt(defaultServer.getPort());

      writeTag(4, 0);
      writeBoolean(defaultServer.getEnable());

      writeTag(63, 3);
   }

   @Override
   public void visitDomain(Domain domain) {
      if (domain.getId() != null) {
         writeTag(1, 1);
         writeString(domain.getId());
      }

      if (!domain.getGroups().isEmpty()) {
         writeTag(33, 2);
         writeInt(domain.getGroups().size());

         for (Group group : domain.getGroups().values()) {
            group.accept(m_visitor);
         }
      }

      writeTag(63, 3);
   }

   @Override
   public void visitGroup(Group group) {
      if (group.getId() != null) {
         writeTag(1, 1);
         writeString(group.getId());
      }

      if (!group.getServers().isEmpty()) {
         writeTag(33, 2);
         writeInt(group.getServers().size());

         for (Server server : group.getServers()) {
            server.accept(m_visitor);
         }
      }

      writeTag(63, 3);
   }

   @Override
   public void visitGroupServer(GroupServer groupServer) {
      if (groupServer.getId() != null) {
         writeTag(1, 1);
         writeString(groupServer.getId());
      }

      writeTag(63, 3);
   }

   @Override
   public void visitNetwork(Network network) {
      if (network.getId() != null) {
         writeTag(1, 1);
         writeString(network.getId());
      }

      writeTag(63, 3);
   }

   @Override
   public void visitNetworkPolicy(NetworkPolicy networkPolicy) {
      if (networkPolicy.getId() != null) {
         writeTag(1, 1);
         writeString(networkPolicy.getId());
      }

      if (networkPolicy.getTitle() != null) {
         writeTag(2, 1);
         writeString(networkPolicy.getTitle());
      }

      writeTag(3, 0);
      writeBoolean(networkPolicy.getBlock());

      if (networkPolicy.getServerGroup() != null) {
         writeTag(4, 1);
         writeString(networkPolicy.getServerGroup());
      }

      if (!networkPolicy.getNetworks().isEmpty()) {
         writeTag(33, 2);
         writeInt(networkPolicy.getNetworks().size());

         for (Network network : networkPolicy.getNetworks().values()) {
            network.accept(m_visitor);
         }
      }

      writeTag(63, 3);
   }

   @Override
   public void visitRouterConfig(RouterConfig routerConfig) {
      writeTag(63, 0);

      if (routerConfig.getBackupServer() != null) {
         writeTag(1, 1);
         writeString(routerConfig.getBackupServer());
      }

      writeTag(2, 0);
      writeInt(routerConfig.getBackupServerPort());

      if (routerConfig.getStartTime() != null) {
         writeTag(3, 1);
         writeDate(routerConfig.getStartTime());
      }

      if (routerConfig.getDomain() != null) {
         writeTag(4, 1);
         writeString(routerConfig.getDomain());
      }

      if (routerConfig.getEndTime() != null) {
         writeTag(5, 1);
         writeDate(routerConfig.getEndTime());
      }

      if (!routerConfig.getDefaultServers().isEmpty()) {
         writeTag(33, 2);
         writeInt(routerConfig.getDefaultServers().size());

         for (DefaultServer defaultServer : routerConfig.getDefaultServers().values()) {
            defaultServer.accept(m_visitor);
         }
      }

      if (!routerConfig.getNetworkPolicies().isEmpty()) {
         writeTag(34, 2);
         writeInt(routerConfig.getNetworkPolicies().size());

         for (NetworkPolicy networkPolicy : routerConfig.getNetworkPolicies().values()) {
            networkPolicy.accept(m_visitor);
         }
      }

      if (!routerConfig.getServerGroups().isEmpty()) {
         writeTag(35, 2);
         writeInt(routerConfig.getServerGroups().size());

         for (ServerGroup serverGroup : routerConfig.getServerGroups().values()) {
            serverGroup.accept(m_visitor);
         }
      }

      if (!routerConfig.getDomains().isEmpty()) {
         writeTag(36, 2);
         writeInt(routerConfig.getDomains().size());

         for (Domain domain : routerConfig.getDomains().values()) {
            domain.accept(m_visitor);
         }
      }

      writeTag(63, 3);
   }

   @Override
   public void visitServer(Server server) {
      if (server.getId() != null) {
         writeTag(1, 1);
         writeString(server.getId());
      }

      writeTag(2, 0);
      writeInt(server.getPort());

      writeTag(3, 0);
      writeDouble(server.getWeight());

      writeTag(63, 3);
   }

   @Override
   public void visitServerGroup(ServerGroup serverGroup) {
      if (serverGroup.getId() != null) {
         writeTag(1, 1);
         writeString(serverGroup.getId());
      }

      if (serverGroup.getTitle() != null) {
         writeTag(2, 1);
         writeString(serverGroup.getTitle());
      }

      if (!serverGroup.getGroupServers().isEmpty()) {
         writeTag(33, 2);
         writeInt(serverGroup.getGroupServers().size());

         for (GroupServer groupServer : serverGroup.getGroupServers().values()) {
            groupServer.accept(m_visitor);
         }
      }

      writeTag(63, 3);
   }

   private void writeBoolean(boolean value) {
      try {
         m_out.writeByte(value ? 1 : 0);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void writeDate(java.util.Date value) {
      try {
         writeVarint(value.getTime());
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void writeDouble(double value) {
      try {
         writeVarint(Double.doubleToLongBits(value));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void writeInt(int value) {
      try {
         writeVarint(value & 0xFFFFFFFFL);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void writeString(String value) {
      try {
         m_out.writeUTF(value);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void writeTag(int field, int type) {
      try {
         m_out.writeByte((field << 2) + type);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   protected void writeVarint(long value) throws IOException {
      while (true) {
         if ((value & ~0x7FL) == 0) {
            m_out.writeByte((byte) value);
            return;
         } else {
            m_out.writeByte(((byte) value & 0x7F) | 0x80);
            value >>>= 7;
         }
      }
   }
}
