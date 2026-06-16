package com.dianping.cat.home.router.transform;

import static com.dianping.cat.home.router.Constants.ATTR_BACKUP_SERVER;
import static com.dianping.cat.home.router.Constants.ATTR_BACKUP_SERVER_PORT;
import static com.dianping.cat.home.router.Constants.ATTR_BLOCK;
import static com.dianping.cat.home.router.Constants.ATTR_DOMAIN;
import static com.dianping.cat.home.router.Constants.ATTR_ENABLE;
import static com.dianping.cat.home.router.Constants.ATTR_ENDTIME;
import static com.dianping.cat.home.router.Constants.ATTR_ID;
import static com.dianping.cat.home.router.Constants.ATTR_PORT;
import static com.dianping.cat.home.router.Constants.ATTR_SERVER_GROUP;
import static com.dianping.cat.home.router.Constants.ATTR_STARTTIME;
import static com.dianping.cat.home.router.Constants.ATTR_TITLE;
import static com.dianping.cat.home.router.Constants.ATTR_WEIGHT;
import static com.dianping.cat.home.router.Constants.ENTITY_DEFAULT_SERVERS;
import static com.dianping.cat.home.router.Constants.ENTITY_DOMAINS;
import static com.dianping.cat.home.router.Constants.ENTITY_GROUPS;
import static com.dianping.cat.home.router.Constants.ENTITY_GROUP_SERVERS;
import static com.dianping.cat.home.router.Constants.ENTITY_NETWORKS;
import static com.dianping.cat.home.router.Constants.ENTITY_NETWORK_POLICIES;
import static com.dianping.cat.home.router.Constants.ENTITY_SERVERS;
import static com.dianping.cat.home.router.Constants.ENTITY_SERVER_GROUPS;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.dianping.cat.home.router.IEntity;
import com.dianping.cat.home.router.entity.DefaultServer;
import com.dianping.cat.home.router.entity.Domain;
import com.dianping.cat.home.router.entity.Group;
import com.dianping.cat.home.router.entity.GroupServer;
import com.dianping.cat.home.router.entity.Network;
import com.dianping.cat.home.router.entity.NetworkPolicy;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.entity.Server;
import com.dianping.cat.home.router.entity.ServerGroup;

public class DefaultJsonParser {

   private DefaultLinker m_linker = new DefaultLinker(true);

   private Stack<String> m_tags = new Stack<String>();

   private Stack<Object> m_objs = new Stack<Object>();

   private List<Object> m_entities = new ArrayList<Object>();

   private Class<?> m_entityClass;

   private DefaultJsonParser(Class<?> entityClass) {
      m_entityClass = entityClass;
   }

   public static <T extends IEntity<T>> T parse(Class<T> entityClass, InputStream in) throws IOException {
      return parse(entityClass, new InputStreamReader(in, "utf-8"));
   }

   @SuppressWarnings("unchecked")
      public static <T extends IEntity<T>> T parse(Class<T> entityClass, Reader reader) throws IOException {
      DefaultJsonParser parser = new DefaultJsonParser(entityClass);

      parser.onArrayBegin();
      parser.parse(new JsonReader(reader));
      parser.onArrayEnd();

      if (parser.m_entities.isEmpty()) {
         return null;
      } else {
         return (T) parser.m_entities.get(0);
      }
   }

   public static <T extends IEntity<T>> T parse(Class<T> entityClass, String json) throws IOException {
      return parse(entityClass, new StringReader(json));
   }

   public static <T extends IEntity<T>> List<T> parseArray(Class<T> entityClass, InputStream in) throws Exception {
      return parseArray(entityClass, new InputStreamReader(in, "utf-8"));
   }

   @SuppressWarnings("unchecked")
   public static <T extends IEntity<T>> List<T> parseArray(Class<T> entityClass, Reader reader) throws Exception {
      DefaultJsonParser parser = new DefaultJsonParser(entityClass);

      parser.parse(new JsonReader(reader));
      return (List<T>) (List<?>) parser.m_entities;
   }

   public static <T extends IEntity<T>> List<T> parseArray(Class<T> entityClass, String json) throws Exception {
      return parseArray(entityClass, new StringReader(json));
   }


   @SuppressWarnings("unchecked")
   protected <T> T convert(Class<T> type, String value, T defaultValue) {
      if (value == null || value.length() == 0) {
         return defaultValue;
      }

      if (type == Boolean.class) {
         return (T) Boolean.valueOf(value);
      } else if (type == Integer.class) {
         return (T) Integer.valueOf(value);
      } else if (type == Long.class) {
         return (T) Long.valueOf(value);
      } else if (type == Short.class) {
         return (T) Short.valueOf(value);
      } else if (type == Float.class) {
         return (T) Float.valueOf(value);
      } else if (type == Double.class) {
         return (T) Double.valueOf(value);
      } else if (type == Byte.class) {
         return (T) Byte.valueOf(value);
      } else if (type == Character.class) {
         return (T) (Character) value.charAt(0);
      } else {
         return (T) value;
      }
   }

   private Object createRootEntity() {
      try {
         Object entity = m_entityClass.newInstance();

         return entity;
      } catch (Exception e) {
         throw new RuntimeException(String.format("Unable to create entity(%s) instance!", m_entityClass.getName()), e);
      }
   }

   private boolean isTopLevel() {
      return m_objs.size() == 1;
   }

   protected void onArrayBegin() {
      if (m_objs.isEmpty()) {
         m_objs.push(m_entities);
         m_tags.push("");
      } else {
         Object parent = m_objs.peek();
         String tag = m_tags.peek();

         if (parent instanceof Group) {
            if (ENTITY_SERVERS.equals(tag)) {
               m_objs.push(parent);
            } else {
               throw new RuntimeException(String.format("Unknown tag(%s) found at %s!", tag, m_tags));
            }
         } else {
            throw new RuntimeException(String.format("Unknown tag(%s) found at %s!", tag, m_tags));
         }
      }   }

   protected void onArrayEnd() {
      m_objs.pop();
      m_tags.pop();

   }
   protected void onName(String name) {
      m_tags.push(name);
   }

   protected void onObjectBegin() {
      if (isTopLevel()) {
         m_objs.push(createRootEntity());
         m_tags.push("");
      } else {
         Object parent = m_objs.peek();
         String tag = m_tags.peek();

         if (parent instanceof RouterConfig) {
            if (ENTITY_DEFAULT_SERVERS.equals(tag) || ENTITY_NETWORK_POLICIES.equals(tag) || ENTITY_SERVER_GROUPS.equals(tag) || ENTITY_DOMAINS.equals(tag)) {
               m_objs.push(parent);
            } else {
               String parentTag = m_tags.size() >= 2 ? m_tags.get(m_tags.size() - 2) : null;

               if (ENTITY_DEFAULT_SERVERS.equals(parentTag)) {
                  DefaultServer defaultServers = new DefaultServer();

                  m_linker.onDefaultServer((RouterConfig) parent, defaultServers);
                  m_objs.push(defaultServers);
               } else if (ENTITY_NETWORK_POLICIES.equals(parentTag)) {
                  NetworkPolicy networkPolicies = new NetworkPolicy();

                  m_linker.onNetworkPolicy((RouterConfig) parent, networkPolicies);
                  m_objs.push(networkPolicies);
               } else if (ENTITY_SERVER_GROUPS.equals(parentTag)) {
                  ServerGroup serverGroups = new ServerGroup();

                  m_linker.onServerGroup((RouterConfig) parent, serverGroups);
                  m_objs.push(serverGroups);
               } else if (ENTITY_DOMAINS.equals(parentTag)) {
                  Domain domains = new Domain();

                  m_linker.onDomain((RouterConfig) parent, domains);
                  m_objs.push(domains);
               } else {
                  throw new RuntimeException(String.format("Unknown tag(%s) found at %s!", tag, m_tags));
               }
            }
         } else if (parent instanceof NetworkPolicy) {
            if (ENTITY_NETWORKS.equals(tag)) {
               m_objs.push(parent);
            } else {
               String parentTag = m_tags.size() >= 2 ? m_tags.get(m_tags.size() - 2) : null;

               if (ENTITY_NETWORKS.equals(parentTag)) {
                  Network networks = new Network();

                  m_linker.onNetwork((NetworkPolicy) parent, networks);
                  m_objs.push(networks);
               } else {
                  throw new RuntimeException(String.format("Unknown tag(%s) found at %s!", tag, m_tags));
               }
            }
         } else if (parent instanceof ServerGroup) {
            if (ENTITY_GROUP_SERVERS.equals(tag)) {
               m_objs.push(parent);
            } else {
               String parentTag = m_tags.size() >= 2 ? m_tags.get(m_tags.size() - 2) : null;

               if (ENTITY_GROUP_SERVERS.equals(parentTag)) {
                  GroupServer groupServers = new GroupServer();

                  m_linker.onGroupServer((ServerGroup) parent, groupServers);
                  m_objs.push(groupServers);
               } else {
                  throw new RuntimeException(String.format("Unknown tag(%s) found at %s!", tag, m_tags));
               }
            }
         } else if (parent instanceof Domain) {
            if (ENTITY_GROUPS.equals(tag)) {
               m_objs.push(parent);
            } else {
               String parentTag = m_tags.size() >= 2 ? m_tags.get(m_tags.size() - 2) : null;

               if (ENTITY_GROUPS.equals(parentTag)) {
                  Group groups = new Group();

                  m_linker.onGroup((Domain) parent, groups);
                  m_objs.push(groups);
               } else {
                  throw new RuntimeException(String.format("Unknown tag(%s) found at %s!", tag, m_tags));
               }
            }
         } else if (parent instanceof Group) {
            if (ENTITY_SERVERS.equals(tag)) {
               Server servers = new Server();

               m_linker.onServer((Group) parent, servers);
               m_objs.push(servers);
               m_tags.push("");
            } else {
               throw new RuntimeException(String.format("Unknown tag(%s) found at %s!", tag, m_tags)); ///
            }
         } else {
            throw new RuntimeException(String.format("Unknown tag(%s) found at %s!", tag, m_tags));
         }
      }
   }

   protected void onObjectEnd() {
      m_tags.pop();

      Object entity = m_objs.pop();

      if (isTopLevel()) {
         m_entities.add(entity);
      }
   }

   protected void onValue(String value) {
      Object parent = m_objs.peek();
      String tag = m_tags.pop();

      if (parent instanceof RouterConfig) {
         parseForRouterConfig((RouterConfig) parent, tag, value);
      } else if (parent instanceof DefaultServer) {
         parseForDefaultServer((DefaultServer) parent, tag, value);
      } else if (parent instanceof NetworkPolicy) {
         parseForNetworkPolicy((NetworkPolicy) parent, tag, value);
      } else if (parent instanceof Network) {
         parseForNetwork((Network) parent, tag, value);
      } else if (parent instanceof ServerGroup) {
         parseForServerGroup((ServerGroup) parent, tag, value);
      } else if (parent instanceof GroupServer) {
         parseForGroupServer((GroupServer) parent, tag, value);
      } else if (parent instanceof Domain) {
         parseForDomain((Domain) parent, tag, value);
      } else if (parent instanceof Group) {
         parseForGroup((Group) parent, tag, value);
      } else if (parent instanceof Server) {
         parseForServer((Server) parent, tag, value);
      } else {
         throw new RuntimeException(String.format("Unknown tag(%s) under %s!", tag, parent));
      }
   }

   private void parse(JsonReader reader) throws IOException {
      try {
         reader.parse(this);
      } catch (EOFException e) {
         if (m_objs.size() > 1) {
            throw new EOFException(String.format("Unexpected end while parsing json! tags: %s.", m_tags));
         }
      }

      m_linker.finish();
   }

   public void parseForDefaultServer(DefaultServer defaultServer, String tag, String value) {
      if (ATTR_ID.equals(tag)) {
         defaultServer.setId(value);
      } else if (ATTR_WEIGHT.equals(tag)) {
         defaultServer.setWeight(convert(Double.class, value, 0.0));
      } else if (ATTR_PORT.equals(tag)) {
         defaultServer.setPort(convert(Integer.class, value, 0));
      } else if (ATTR_ENABLE.equals(tag)) {
         defaultServer.setEnable(convert(Boolean.class, value, false));
      } else {
         throw new RuntimeException(String.format("Unknown tag(%s) of %s under %s!", tag, defaultServer, m_tags));
      }
   }

   public void parseForDomain(Domain domain, String tag, String value) {
      if (ENTITY_GROUPS.equals(tag)) {
         // do nothing here
      } else if (ATTR_ID.equals(tag)) {
         domain.setId(value);
      } else {
         throw new RuntimeException(String.format("Unknown tag(%s) of %s under %s!", tag, domain, m_tags));
      }
   }

   public void parseForGroup(Group group, String tag, String value) {
      if (ENTITY_SERVERS.equals(tag)) {
         // do nothing here
      } else if (ATTR_ID.equals(tag)) {
         group.setId(value);
      } else {
         throw new RuntimeException(String.format("Unknown tag(%s) of %s under %s!", tag, group, m_tags));
      }
   }

   public void parseForGroupServer(GroupServer groupServer, String tag, String value) {
      if (ATTR_ID.equals(tag)) {
         groupServer.setId(value);
      } else {
         throw new RuntimeException(String.format("Unknown tag(%s) of %s under %s!", tag, groupServer, m_tags));
      }
   }

   public void parseForNetwork(Network network, String tag, String value) {
      if (ATTR_ID.equals(tag)) {
         network.setId(value);
      } else {
         throw new RuntimeException(String.format("Unknown tag(%s) of %s under %s!", tag, network, m_tags));
      }
   }

   public void parseForNetworkPolicy(NetworkPolicy networkPolicy, String tag, String value) {
      if (ENTITY_NETWORKS.equals(tag)) {
         // do nothing here
      } else if (ATTR_ID.equals(tag)) {
         networkPolicy.setId(value);
      } else if (ATTR_TITLE.equals(tag)) {
         networkPolicy.setTitle(value);
      } else if (ATTR_BLOCK.equals(tag)) {
         networkPolicy.setBlock(convert(Boolean.class, value, false));
      } else if (ATTR_SERVER_GROUP.equals(tag)) {
         networkPolicy.setServerGroup(value);
      } else {
         throw new RuntimeException(String.format("Unknown tag(%s) of %s under %s!", tag, networkPolicy, m_tags));
      }
   }

   public void parseForRouterConfig(RouterConfig routerConfig, String tag, String value) {
      if (ENTITY_DEFAULT_SERVERS.equals(tag) || ENTITY_NETWORK_POLICIES.equals(tag) || ENTITY_SERVER_GROUPS.equals(tag) || ENTITY_DOMAINS.equals(tag)) {
         // do nothing here
      } else if (ATTR_BACKUP_SERVER.equals(tag)) {
         routerConfig.setBackupServer(value);
      } else if (ATTR_BACKUP_SERVER_PORT.equals(tag)) {
         routerConfig.setBackupServerPort(convert(Integer.class, value, 0));
      } else if (ATTR_STARTTIME.equals(tag)) {
         routerConfig.setStartTime(toDate(value, "yyyy-MM-dd HH:mm:ss"));
      } else if (ATTR_DOMAIN.equals(tag)) {
         routerConfig.setDomain(value);
      } else if (ATTR_ENDTIME.equals(tag)) {
         routerConfig.setEndTime(toDate(value, "yyyy-MM-dd HH:mm:ss"));
      } else {
         throw new RuntimeException(String.format("Unknown tag(%s) of %s under %s!", tag, routerConfig, m_tags));
      }
   }

   public void parseForServer(Server server, String tag, String value) {
      if (ATTR_ID.equals(tag)) {
         server.setId(value);
      } else if (ATTR_PORT.equals(tag)) {
         server.setPort(convert(Integer.class, value, 0));
      } else if (ATTR_WEIGHT.equals(tag)) {
         server.setWeight(convert(Double.class, value, 0.0));
      } else {
         throw new RuntimeException(String.format("Unknown tag(%s) of %s under %s!", tag, server, m_tags));
      }
   }

   public void parseForServerGroup(ServerGroup serverGroup, String tag, String value) {
      if (ENTITY_GROUP_SERVERS.equals(tag)) {
         // do nothing here
      } else if (ATTR_ID.equals(tag)) {
         serverGroup.setId(value);
      } else if (ATTR_TITLE.equals(tag)) {
         serverGroup.setTitle(value);
      } else {
         throw new RuntimeException(String.format("Unknown tag(%s) of %s under %s!", tag, serverGroup, m_tags));
      }
   }


   protected java.util.Date toDate(String str, String format) {
      try {
         return new java.text.SimpleDateFormat(format).parse(str);
      } catch (java.text.ParseException e) {
         throw new RuntimeException(String.format("Unable to parse date(%s) in format(%s)!", str, format), e);
      }
   }

   static class JsonReader {
      private Reader m_reader;

      private char[] m_buffer = new char[2048];

      private int m_size;

      private int m_index;

      public JsonReader(Reader reader) {
         m_reader = reader;
      }

      private char next() throws IOException {
         if (m_index >= m_size) {
            m_size = m_reader.read(m_buffer);
            m_index = 0;

            if (m_size == -1) {
               throw new EOFException();
            }
         }

         return m_buffer[m_index++];
      }

      public void parse(DefaultJsonParser parser) throws IOException {
         StringBuilder sb = new StringBuilder();
         boolean flag = false;

         while (true) {
            char ch = next();

            switch (ch) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
               break;
            case '{':
               parser.onObjectBegin();
               flag = false;
               break;
            case '}':
               if (flag) { // have value
                  parser.onValue(sb.toString());
                  sb.setLength(0);
               }

               parser.onObjectEnd();
               flag = false;
               break;
            case '\'':
            case '"':
               while (true) {
                  char ch2 = next();

                  if (ch2 != ch) {
                     if (ch2 == '\\') {
                        char ch3 = next();

                        switch (ch3) {
                        case 't':
                        	sb.append('\t');
                        	break;
                        case 'r':
                        	sb.append('\r');
                        	break;
                        case 'n':
                        	sb.append('\n');
                        	break;
                        default:
                        	sb.append(ch3);
                        	break;
                        }
                     } else {
                        sb.append(ch2);
                     }
                  } else {
                     if (!flag) {
                        parser.onName(sb.toString());
                     } else {
                        parser.onValue(sb.toString());
                        flag = false;
                     }

                     sb.setLength(0);
                     break;
                  }
               }

               break;
            case ':':
               if (sb.length() != 0) {
                  parser.onName(sb.toString());
                  sb.setLength(0);
               }

               flag = true;
               break;
            case ',':
               if (sb.length() != 0) {
                  if (!flag) {
                     parser.onName(sb.toString());
                  } else {
                     parser.onValue(sb.toString());
                  }

                  sb.setLength(0);
               }

               flag = false;
               break;
            case '[':
               parser.onArrayBegin();
               flag = false;
               break;
            case ']':
               parser.onArrayEnd();
               flag = false;
               break;
            default:
               sb.append(ch);
               break;
            }
         }
      }
   }
}
