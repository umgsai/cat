package com.dianping.cat.home.router.transform;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

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

public class DefaultNativeParser implements IVisitor {

   private DefaultLinker m_linker = new DefaultLinker(true);

   private DataInputStream m_in;

   public DefaultNativeParser(InputStream in) {
      m_in = new DataInputStream(in);
   }

   public static RouterConfig parse(byte[] data) {
      return parse(new ByteArrayInputStream(data));
   }

   public static RouterConfig parse(InputStream in) {
      DefaultNativeParser parser = new DefaultNativeParser(in);
      RouterConfig routerConfig = new RouterConfig();

      try {
         routerConfig.accept(parser);
      } catch (RuntimeException e) {
         if (e.getCause() !=null && e.getCause() instanceof java.io.EOFException) {
            // ignore it
         } else {
            throw e;
         }
      }
      
      parser.m_linker.finish();
      return routerConfig;
   }

   @Override
   public void visitDefaultServer(DefaultServer defaultServer) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitDefaultServerChildren(defaultServer, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitDefaultServerChildren(DefaultServer defaultServer, int _field, int _type) {
      switch (_field) {
         case 1:
            defaultServer.setId(readString());
            break;
         case 2:
            defaultServer.setWeight(readDouble());
            break;
         case 3:
            defaultServer.setPort(readInt());
            break;
         case 4:
            defaultServer.setEnable(readBoolean());
            break;
      }
   }

   @Override
   public void visitDomain(Domain domain) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitDomainChildren(domain, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitDomainChildren(Domain domain, int _field, int _type) {
      switch (_field) {
         case 1:
            domain.setId(readString());
            break;
         case 33:
            if (_type == 1) { 
              Group group = new Group();

              visitGroup(group);
              m_linker.onGroup(domain, group);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 Group group = new Group();

                 visitGroup(group);
                 m_linker.onGroup(domain, group);
               }
            }
            break;
      }
   }

   @Override
   public void visitGroup(Group group) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitGroupChildren(group, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitGroupChildren(Group group, int _field, int _type) {
      switch (_field) {
         case 1:
            group.setId(readString());
            break;
         case 33:
            if (_type == 1) { 
              Server server = new Server();

              visitServer(server);
              m_linker.onServer(group, server);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 Server server = new Server();

                 visitServer(server);
                 m_linker.onServer(group, server);
               }
            }
            break;
      }
   }

   @Override
   public void visitGroupServer(GroupServer groupServer) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitGroupServerChildren(groupServer, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitGroupServerChildren(GroupServer groupServer, int _field, int _type) {
      switch (_field) {
         case 1:
            groupServer.setId(readString());
            break;
      }
   }

   @Override
   public void visitNetwork(Network network) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitNetworkChildren(network, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitNetworkChildren(Network network, int _field, int _type) {
      switch (_field) {
         case 1:
            network.setId(readString());
            break;
      }
   }

   @Override
   public void visitNetworkPolicy(NetworkPolicy networkPolicy) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitNetworkPolicyChildren(networkPolicy, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitNetworkPolicyChildren(NetworkPolicy networkPolicy, int _field, int _type) {
      switch (_field) {
         case 1:
            networkPolicy.setId(readString());
            break;
         case 2:
            networkPolicy.setTitle(readString());
            break;
         case 3:
            networkPolicy.setBlock(readBoolean());
            break;
         case 4:
            networkPolicy.setServerGroup(readString());
            break;
         case 33:
            if (_type == 1) { 
              Network network = new Network();

              visitNetwork(network);
              m_linker.onNetwork(networkPolicy, network);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 Network network = new Network();

                 visitNetwork(network);
                 m_linker.onNetwork(networkPolicy, network);
               }
            }
            break;
      }
   }

   @Override
   public void visitRouterConfig(RouterConfig routerConfig) {
      byte tag;

      if ((tag = readTag()) != -4) {
         throw new RuntimeException(String.format("Malformed payload, expected: %s, but was: %s!", -4, tag));
      }

      while ((tag = readTag()) != -1) {
         visitRouterConfigChildren(routerConfig, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitRouterConfigChildren(RouterConfig routerConfig, int _field, int _type) {
      switch (_field) {
         case 1:
            routerConfig.setBackupServer(readString());
            break;
         case 2:
            routerConfig.setBackupServerPort(readInt());
            break;
         case 3:
            routerConfig.setStartTime(readDate());
            break;
         case 4:
            routerConfig.setDomain(readString());
            break;
         case 5:
            routerConfig.setEndTime(readDate());
            break;
         case 33:
            if (_type == 1) { 
              DefaultServer defaultServer = new DefaultServer();

              visitDefaultServer(defaultServer);
              m_linker.onDefaultServer(routerConfig, defaultServer);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 DefaultServer defaultServer = new DefaultServer();

                 visitDefaultServer(defaultServer);
                 m_linker.onDefaultServer(routerConfig, defaultServer);
               }
            }
            break;
         case 34:
            if (_type == 1) { 
              NetworkPolicy networkPolicy = new NetworkPolicy();

              visitNetworkPolicy(networkPolicy);
              m_linker.onNetworkPolicy(routerConfig, networkPolicy);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 NetworkPolicy networkPolicy = new NetworkPolicy();

                 visitNetworkPolicy(networkPolicy);
                 m_linker.onNetworkPolicy(routerConfig, networkPolicy);
               }
            }
            break;
         case 35:
            if (_type == 1) { 
              ServerGroup serverGroup = new ServerGroup();

              visitServerGroup(serverGroup);
              m_linker.onServerGroup(routerConfig, serverGroup);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 ServerGroup serverGroup = new ServerGroup();

                 visitServerGroup(serverGroup);
                 m_linker.onServerGroup(routerConfig, serverGroup);
               }
            }
            break;
         case 36:
            if (_type == 1) { 
              Domain domain = new Domain();

              visitDomain(domain);
              m_linker.onDomain(routerConfig, domain);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 Domain domain = new Domain();

                 visitDomain(domain);
                 m_linker.onDomain(routerConfig, domain);
               }
            }
            break;
      }
   }

   @Override
   public void visitServer(Server server) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitServerChildren(server, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitServerChildren(Server server, int _field, int _type) {
      switch (_field) {
         case 1:
            server.setId(readString());
            break;
         case 2:
            server.setPort(readInt());
            break;
         case 3:
            server.setWeight(readDouble());
            break;
      }
   }

   @Override
   public void visitServerGroup(ServerGroup serverGroup) {
      byte tag;

      while ((tag = readTag()) != -1) {
         visitServerGroupChildren(serverGroup, (tag & 0xFF) >> 2, tag & 0x3);
      }
   }

   protected void visitServerGroupChildren(ServerGroup serverGroup, int _field, int _type) {
      switch (_field) {
         case 1:
            serverGroup.setId(readString());
            break;
         case 2:
            serverGroup.setTitle(readString());
            break;
         case 33:
            if (_type == 1) { 
              GroupServer groupServer = new GroupServer();

              visitGroupServer(groupServer);
              m_linker.onGroupServer(serverGroup, groupServer);
            } else if (_type == 2) {
               for (int i = readInt(); i > 0; i--) {
                 GroupServer groupServer = new GroupServer();

                 visitGroupServer(groupServer);
                 m_linker.onGroupServer(serverGroup, groupServer);
               }
            }
            break;
      }
   }

   private boolean readBoolean() {
      try {
         return m_in.readByte() == 1 ? Boolean.TRUE : Boolean.FALSE;
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private java.util.Date readDate() {
      try {
         return new java.util.Date(readVarint(64));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private double readDouble() {
      try {
         return Double.longBitsToDouble(readVarint(64));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private int readInt() {
      try {
         return (int) readVarint(32);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private String readString() {
      try {
         return m_in.readUTF();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private byte readTag() {
      try {
         return m_in.readByte();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   protected long readVarint(final int length) throws IOException {
      int shift = 0;
      long result = 0;

      while (shift < length) {
         final byte b = m_in.readByte();
         result |= (long) (b & 0x7F) << shift;
         if ((b & 0x80) == 0) {
            return result;
         }
         shift += 7;
      }

      throw new RuntimeException("Malformed variable int " + length + "!");
   }
}
