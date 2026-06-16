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
import static com.dianping.cat.home.router.Constants.ENTITY_DEFAULT_SERVER;
import static com.dianping.cat.home.router.Constants.ENTITY_DOMAIN;
import static com.dianping.cat.home.router.Constants.ENTITY_GROUP;
import static com.dianping.cat.home.router.Constants.ENTITY_GROUP_SERVER;
import static com.dianping.cat.home.router.Constants.ENTITY_NETWORK;
import static com.dianping.cat.home.router.Constants.ENTITY_NETWORK_POLICY;
import static com.dianping.cat.home.router.Constants.ENTITY_ROUTER_CONFIG;
import static com.dianping.cat.home.router.Constants.ENTITY_SERVER;
import static com.dianping.cat.home.router.Constants.ENTITY_SERVER_GROUP;

import java.lang.reflect.Array;
import java.util.Collection;

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

public class DefaultXmlBuilder implements IVisitor {

   private IVisitor m_visitor = this;

   private int m_level;

   private StringBuilder m_sb;

   private boolean m_compact;

   public DefaultXmlBuilder() {
      this(false);
   }

   public DefaultXmlBuilder(boolean compact) {
      this(compact, new StringBuilder(4096));
   }

   public DefaultXmlBuilder(boolean compact, StringBuilder sb) {
      m_compact = compact;
      m_sb = sb;
      m_sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
   }

   public String buildXml(IEntity<?> entity) {
      entity.accept(m_visitor);
      return m_sb.toString();
   }

   protected void endTag(String name) {
      m_level--;

      indent();
      m_sb.append("</").append(name).append(">\r\n");
   }

   protected String escape(Object value) {
      return escape(value, false);
   }
   
   protected String escape(Object value, boolean text) {
      if (value == null) {
         return null;
      }

      String str = toString(value);
      int len = str.length();
      StringBuilder sb = new StringBuilder(len + 16);

      for (int i = 0; i < len; i++) {
         final char ch = str.charAt(i);

         switch (ch) {
         case '<':
            sb.append("&lt;");
            break;
         case '>':
            sb.append("&gt;");
            break;
         case '&':
            sb.append("&amp;");
            break;
         case '"':
            if (!text) {
               sb.append("&quot;");
               break;
            }
         default:
            sb.append(ch);
            break;
         }
      }

      return sb.toString();
   }
   
   protected void indent() {
      if (!m_compact) {
         for (int i = m_level - 1; i >= 0; i--) {
            m_sb.append("   ");
         }
      }
   }

   protected void startTag(String name) {
      startTag(name, false, null);
   }
   
   protected void startTag(String name, boolean closed, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
      startTag(name, null, closed, dynamicAttributes, nameValues);
   }

   protected void startTag(String name, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
      startTag(name, null, false, dynamicAttributes, nameValues);
   }

   protected void startTag(String name, Object text, boolean closed, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
      indent();

      m_sb.append('<').append(name);

      int len = nameValues.length;

      for (int i = 0; i + 1 < len; i += 2) {
         Object attrName = nameValues[i];
         Object attrValue = nameValues[i + 1];

         if (attrValue != null) {
            m_sb.append(' ').append(attrName).append("=\"").append(escape(attrValue)).append('"');
         }
      }

      if (dynamicAttributes != null) {
         for (java.util.Map.Entry<String, String> e : dynamicAttributes.entrySet()) {
            m_sb.append(' ').append(e.getKey()).append("=\"").append(escape(e.getValue())).append('"');
         }
      }

      if (text != null && closed) {
         m_sb.append('>');
         m_sb.append(escape(text, true));
         m_sb.append("</").append(name).append(">\r\n");
      } else {
         if (closed) {
            m_sb.append('/');
         } else {
            m_level++;
         }
   
         m_sb.append(">\r\n");
      }
   }

   @SuppressWarnings("unchecked")
   protected String toString(Object value) {
      if (value instanceof String) {
         return (String) value;
      } else if (value instanceof Collection) {
         Collection<Object> list = (Collection<Object>) value;
         StringBuilder sb = new StringBuilder(32);
         boolean first = true;

         for (Object item : list) {
            if (first) {
               first = false;
            } else {
               sb.append(',');
            }

            if (item != null) {
               sb.append(item);
            }
         }

         return sb.toString();
      } else if (value.getClass().isArray()) {
         int len = Array.getLength(value);
         StringBuilder sb = new StringBuilder(32);
         boolean first = true;

         for (int i = 0; i < len; i++) {
            Object item = Array.get(value, i);

            if (first) {
               first = false;
            } else {
               sb.append(',');
            }

            if (item != null) {
               sb.append(item);
            }
         }
		
         return sb.toString();
      }
 
      return String.valueOf(value);
   }

   protected String toString(java.util.Date date, String format) {
      if (date != null) {
         return new java.text.SimpleDateFormat(format).format(date);
      } else {
         return null;
      }
   }

   @Override
   public void visitDefaultServer(DefaultServer defaultServer) {
      startTag(ENTITY_DEFAULT_SERVER, true, null, ATTR_ID, defaultServer.getId(), ATTR_WEIGHT, defaultServer.getWeight(), ATTR_PORT, defaultServer.getPort(), ATTR_ENABLE, defaultServer.isEnable());
   }

   @Override
   public void visitDomain(Domain domain) {
      startTag(ENTITY_DOMAIN, null, ATTR_ID, domain.getId());

      if (!domain.getGroups().isEmpty()) {
         for (Group group : domain.getGroups().values()) {
            group.accept(m_visitor);
         }
      }

      endTag(ENTITY_DOMAIN);
   }

   @Override
   public void visitGroup(Group group) {
      startTag(ENTITY_GROUP, null, ATTR_ID, group.getId());

      if (!group.getServers().isEmpty()) {
         for (Server server : group.getServers()) {
            server.accept(m_visitor);
         }
      }

      endTag(ENTITY_GROUP);
   }

   @Override
   public void visitGroupServer(GroupServer groupServer) {
      startTag(ENTITY_GROUP_SERVER, true, null, ATTR_ID, groupServer.getId());
   }

   @Override
   public void visitNetwork(Network network) {
      startTag(ENTITY_NETWORK, true, null, ATTR_ID, network.getId());
   }

   @Override
   public void visitNetworkPolicy(NetworkPolicy networkPolicy) {
      startTag(ENTITY_NETWORK_POLICY, null, ATTR_ID, networkPolicy.getId(), ATTR_TITLE, networkPolicy.getTitle(), ATTR_BLOCK, networkPolicy.isBlock(), ATTR_SERVER_GROUP, networkPolicy.getServerGroup());

      if (!networkPolicy.getNetworks().isEmpty()) {
         for (Network network : networkPolicy.getNetworks().values()) {
            network.accept(m_visitor);
         }
      }

      endTag(ENTITY_NETWORK_POLICY);
   }

   @Override
   public void visitRouterConfig(RouterConfig routerConfig) {
      startTag(ENTITY_ROUTER_CONFIG, null, ATTR_BACKUP_SERVER, routerConfig.getBackupServer(), ATTR_BACKUP_SERVER_PORT, routerConfig.getBackupServerPort(), ATTR_STARTTIME, toString(routerConfig.getStartTime(), "yyyy-MM-dd HH:mm:ss"), ATTR_DOMAIN, routerConfig.getDomain(), ATTR_ENDTIME, toString(routerConfig.getEndTime(), "yyyy-MM-dd HH:mm:ss"));

      if (!routerConfig.getDefaultServers().isEmpty()) {
         for (DefaultServer defaultServer : routerConfig.getDefaultServers().values()) {
            defaultServer.accept(m_visitor);
         }
      }

      if (!routerConfig.getNetworkPolicies().isEmpty()) {
         for (NetworkPolicy networkPolicy : routerConfig.getNetworkPolicies().values()) {
            networkPolicy.accept(m_visitor);
         }
      }

      if (!routerConfig.getServerGroups().isEmpty()) {
         for (ServerGroup serverGroup : routerConfig.getServerGroups().values()) {
            serverGroup.accept(m_visitor);
         }
      }

      if (!routerConfig.getDomains().isEmpty()) {
         for (Domain domain : routerConfig.getDomains().values()) {
            domain.accept(m_visitor);
         }
      }

      endTag(ENTITY_ROUTER_CONFIG);
   }

   @Override
   public void visitServer(Server server) {
      startTag(ENTITY_SERVER, true, null, ATTR_ID, server.getId(), ATTR_PORT, server.getPort(), ATTR_WEIGHT, server.getWeight());
   }

   @Override
   public void visitServerGroup(ServerGroup serverGroup) {
      startTag(ENTITY_SERVER_GROUP, null, ATTR_ID, serverGroup.getId(), ATTR_TITLE, serverGroup.getTitle());

      if (!serverGroup.getGroupServers().isEmpty()) {
         for (GroupServer groupServer : serverGroup.getGroupServers().values()) {
            groupServer.accept(m_visitor);
         }
      }

      endTag(ENTITY_SERVER_GROUP);
   }
}
