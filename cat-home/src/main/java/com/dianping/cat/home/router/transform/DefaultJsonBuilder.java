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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

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

public class DefaultJsonBuilder implements IVisitor {

   private IVisitor m_visitor;

   private int m_level;

   private StringBuilder m_sb;

   private boolean m_compact;

   public DefaultJsonBuilder() {
      this(false);
   }

   public DefaultJsonBuilder(boolean compact) {
      this(compact, new StringBuilder(4096));
   }

   public DefaultJsonBuilder(boolean compact, StringBuilder sb) {
      m_compact = compact;
      m_sb = sb;
      m_visitor = this;
   }

   protected void arrayBegin(String name) {
      indent();
      m_sb.append('"').append(name).append(m_compact ? "\":[" : "\": [\r\n");
      m_level++;
   }

   protected void arrayEnd(String name) {
      m_level--;

      trimComma();
      indent();
      m_sb.append("],").append(m_compact ? "" : "\r\n");
   }

   protected void attributes(Map<String, String> dynamicAttributes, Object... nameValues) {
      int len = nameValues.length;

      for (int i = 0; i + 1 < len; i += 2) {
         Object attrName = nameValues[i];
         Object attrValue = nameValues[i + 1];

         if (attrValue != null) {
            if (attrValue instanceof Collection) {
               @SuppressWarnings("unchecked")
               Collection<Object> items = (Collection<Object>) attrValue;

               if (!items.isEmpty()) {
                  indent();
                  m_sb.append('"').append(attrName).append(m_compact ? "\":[" : "\": [");

                  for (Object item : items) {
                     m_sb.append(' ');
                     toString(m_sb, item);
                     m_sb.append(',');
                  }

                  m_sb.setLength(m_sb.length() - 1);
                  m_sb.append(m_compact ? "]," : " ],\r\n");
               }
            } else if (attrValue.getClass().isArray()) {
               int length = Array.getLength(attrValue);

               if (length > 0) {
                  indent();
                  m_sb.append('"').append(attrName).append(m_compact ? "\":[" : "\": [");

                  for (int j = 0; j < length; j++) {
                     Object item = Array.get(attrValue, j);
                     m_sb.append(' ');
                     toString(m_sb, item);
                     m_sb.append(',');
                  }

                  m_sb.setLength(m_sb.length() - 1);
                  m_sb.append(m_compact ? "]," : " ],\r\n");
               }
            } else {
               if (m_compact) {
                  m_sb.append('"').append(attrName).append("\":");
                  toString(m_sb, attrValue);
                  m_sb.append(",");
               } else {
                  indent();
                  m_sb.append('"').append(attrName).append("\": ");
                  toString(m_sb, attrValue);
                  m_sb.append(",\r\n");
               }
            }
         }
      }

      if (dynamicAttributes != null) {
         for (Map.Entry<String, String> e : dynamicAttributes.entrySet()) {
            if (m_compact) {
               m_sb.append('"').append(e.getKey()).append("\":");
               toString(m_sb, e.getValue());
               m_sb.append(",");
            } else {
               indent();
               m_sb.append('"').append(e.getKey()).append("\": ");
               toString(m_sb, e.getValue());
               m_sb.append(",\r\n");
            }
         }
      }
   }

   public String build(IEntity<?> entity) {
      objectBegin(null);
      entity.accept(this);
      objectEnd(null);
      trimComma();

      return m_sb.toString();
   }

   public String buildArray(Collection<? extends IEntity<?>> entities) {
      m_sb.append('[');

      if (entities != null) {
         for (IEntity<?> entity : entities) {
            objectBegin(null);
            entity.accept(this);
            objectEnd(null);
         }

         trimComma();
      }

      m_sb.append(']');

      return m_sb.toString();
   }

   protected void indent() {
      if (!m_compact) {
         for (int i = m_level - 1; i >= 0; i--) {
            m_sb.append("   ");
         }
      }
   }

   protected void objectBegin(String name) {
      indent();

      if (name == null) {
         m_sb.append("{").append(m_compact ? "" : "\r\n");
      } else {
         m_sb.append('"').append(name).append(m_compact ? "\":{" : "\": {\r\n");
      }

      m_level++;
   }

   protected void objectEnd(String name) {
      m_level--;

      trimComma();
      indent();
      m_sb.append(m_compact ? "}," : "},\r\n");
   }

   protected String toString(java.util.Date date, String format) {
      if (date != null) {
         return new java.text.SimpleDateFormat(format).format(date);
      } else {
         return null;
      }
   }

   protected void toString(StringBuilder sb, Object value) {
      if (value == null) {
         sb.append("null");
      } else if (value instanceof Boolean || value instanceof Number) {
         sb.append(value);
      } else {
         String val = value.toString();
         int len = val.length();

         sb.append('"');

         for (int i = 0; i < len; i++) {
            char ch = val.charAt(i);

            switch (ch) {
            case '\\':
            case '/':
            case '"':
               sb.append('\\').append(ch);
               break;
            case '\t':
               sb.append("\\t");
               break;
            case '\r':
               sb.append("\\r");
               break;
            case '\n':
               sb.append("\\n");
               break;
            default:
               sb.append(ch);
               break;
            }
         }

         sb.append('"');
      }
   }

   protected void trimComma() {
      int len = m_sb.length();

      if (m_compact) {
         if (len > 1 && m_sb.charAt(len - 1) == ',') {
            m_sb.replace(len - 1, len, "");
         }
      } else {
         if (len > 3 && m_sb.charAt(len - 3) == ',') {
            m_sb.replace(len - 3, len - 2, "");
         }
      }
   }

   @Override
   public void visitDefaultServer(DefaultServer defaultServer) {
      attributes(null, ATTR_ID, defaultServer.getId(), ATTR_WEIGHT, defaultServer.getWeight(), ATTR_PORT, defaultServer.getPort(), ATTR_ENABLE, defaultServer.isEnable());
   }

   @Override
   public void visitDomain(Domain domain) {
      attributes(null, ATTR_ID, domain.getId());

      if (!domain.getGroups().isEmpty()) {
         objectBegin(ENTITY_GROUPS);

         for (Map.Entry<String, Group> e : domain.getGroups().entrySet()) {
            String key = String.valueOf(e.getKey());

            objectBegin(key);
            e.getValue().accept(m_visitor);
            objectEnd(key);
         }

         objectEnd(ENTITY_GROUPS);
      }
   }

   @Override
   public void visitGroup(Group group) {
      attributes(null, ATTR_ID, group.getId());

      if (!group.getServers().isEmpty()) {
         arrayBegin(ENTITY_SERVERS);

         for (Server server : group.getServers()) {
            objectBegin(null);
            server.accept(m_visitor);
            objectEnd(null);
         }

         arrayEnd(ENTITY_SERVERS);
      }
   }

   @Override
   public void visitGroupServer(GroupServer groupServer) {
      attributes(null, ATTR_ID, groupServer.getId());
   }

   @Override
   public void visitNetwork(Network network) {
      attributes(null, ATTR_ID, network.getId());
   }

   @Override
   public void visitNetworkPolicy(NetworkPolicy networkPolicy) {
      attributes(null, ATTR_ID, networkPolicy.getId(), ATTR_TITLE, networkPolicy.getTitle(), ATTR_BLOCK, networkPolicy.isBlock(), ATTR_SERVER_GROUP, networkPolicy.getServerGroup());

      if (!networkPolicy.getNetworks().isEmpty()) {
         objectBegin(ENTITY_NETWORKS);

         for (Map.Entry<String, Network> e : networkPolicy.getNetworks().entrySet()) {
            String key = String.valueOf(e.getKey());

            objectBegin(key);
            e.getValue().accept(m_visitor);
            objectEnd(key);
         }

         objectEnd(ENTITY_NETWORKS);
      }
   }

   @Override
   public void visitRouterConfig(RouterConfig routerConfig) {
      attributes(null, ATTR_BACKUP_SERVER, routerConfig.getBackupServer(), ATTR_BACKUP_SERVER_PORT, routerConfig.getBackupServerPort(), ATTR_STARTTIME, toString(routerConfig.getStartTime(), "yyyy-MM-dd HH:mm:ss"), ATTR_DOMAIN, routerConfig.getDomain(), ATTR_ENDTIME, toString(routerConfig.getEndTime(), "yyyy-MM-dd HH:mm:ss"));

      if (!routerConfig.getDefaultServers().isEmpty()) {
         objectBegin(ENTITY_DEFAULT_SERVERS);

         for (Map.Entry<String, DefaultServer> e : routerConfig.getDefaultServers().entrySet()) {
            String key = String.valueOf(e.getKey());

            objectBegin(key);
            e.getValue().accept(m_visitor);
            objectEnd(key);
         }

         objectEnd(ENTITY_DEFAULT_SERVERS);
      }

      if (!routerConfig.getNetworkPolicies().isEmpty()) {
         objectBegin(ENTITY_NETWORK_POLICIES);

         for (Map.Entry<String, NetworkPolicy> e : routerConfig.getNetworkPolicies().entrySet()) {
            String key = String.valueOf(e.getKey());

            objectBegin(key);
            e.getValue().accept(m_visitor);
            objectEnd(key);
         }

         objectEnd(ENTITY_NETWORK_POLICIES);
      }

      if (!routerConfig.getServerGroups().isEmpty()) {
         objectBegin(ENTITY_SERVER_GROUPS);

         for (Map.Entry<String, ServerGroup> e : routerConfig.getServerGroups().entrySet()) {
            String key = String.valueOf(e.getKey());

            objectBegin(key);
            e.getValue().accept(m_visitor);
            objectEnd(key);
         }

         objectEnd(ENTITY_SERVER_GROUPS);
      }

      if (!routerConfig.getDomains().isEmpty()) {
         objectBegin(ENTITY_DOMAINS);

         for (Map.Entry<String, Domain> e : routerConfig.getDomains().entrySet()) {
            String key = String.valueOf(e.getKey());

            objectBegin(key);
            e.getValue().accept(m_visitor);
            objectEnd(key);
         }

         objectEnd(ENTITY_DOMAINS);
      }
   }

   @Override
   public void visitServer(Server server) {
      attributes(null, ATTR_ID, server.getId(), ATTR_PORT, server.getPort(), ATTR_WEIGHT, server.getWeight());
   }

   @Override
   public void visitServerGroup(ServerGroup serverGroup) {
      attributes(null, ATTR_ID, serverGroup.getId(), ATTR_TITLE, serverGroup.getTitle());

      if (!serverGroup.getGroupServers().isEmpty()) {
         objectBegin(ENTITY_GROUP_SERVERS);

         for (Map.Entry<String, GroupServer> e : serverGroup.getGroupServers().entrySet()) {
            String key = String.valueOf(e.getKey());

            objectBegin(key);
            e.getValue().accept(m_visitor);
            objectEnd(key);
         }

         objectEnd(ENTITY_GROUP_SERVERS);
      }
   }
}
