package com.dianping.cat.home.router.transform;

import static com.dianping.cat.home.router.Constants.ENTITY_DEFAULT_SERVER;
import static com.dianping.cat.home.router.Constants.ENTITY_DOMAIN;
import static com.dianping.cat.home.router.Constants.ENTITY_GROUP;
import static com.dianping.cat.home.router.Constants.ENTITY_GROUP_SERVER;
import static com.dianping.cat.home.router.Constants.ENTITY_NETWORK;
import static com.dianping.cat.home.router.Constants.ENTITY_NETWORK_POLICY;
import static com.dianping.cat.home.router.Constants.ENTITY_ROUTER_CONFIG;
import static com.dianping.cat.home.router.Constants.ENTITY_SERVER;
import static com.dianping.cat.home.router.Constants.ENTITY_SERVER_GROUP;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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

public class DefaultSaxParser extends DefaultHandler {

   private DefaultLinker m_linker = new DefaultLinker(true);

   private DefaultSaxMaker m_maker = new DefaultSaxMaker();

   private Stack<String> m_tags = new Stack<String>();

   private Stack<Object> m_objs = new Stack<Object>();

   private IEntity<?> m_entity;

   private StringBuilder m_text = new StringBuilder();

   public static RouterConfig parse(InputStream in) throws SAXException, IOException {
      return parseEntity(RouterConfig.class, new InputSource(removeBOM(in)));
   }

   public static RouterConfig parse(Reader reader) throws SAXException, IOException {
      return parseEntity(RouterConfig.class, new InputSource(removeBOM(reader)));
   }

   public static RouterConfig parse(String xml) throws SAXException, IOException {
      return parseEntity(RouterConfig.class, new InputSource(new StringReader(removeBOM(xml))));
   }

   @SuppressWarnings("unchecked")
   private static <T extends IEntity<?>> T parseEntity(Class<T> type, InputSource is) throws SAXException, IOException {
      try {
         DefaultSaxParser handler = new DefaultSaxParser();
         SAXParserFactory factory = SAXParserFactory.newInstance();

         factory.setValidating(false);
         factory.setFeature("http://xml.org/sax/features/validation", false);

         factory.newSAXParser().parse(is, handler);
         return (T) handler.getEntity();
      } catch (ParserConfigurationException e) {
         throw new IllegalStateException("Unable to get SAX parser instance!", e);
      }
   }

   public static <T extends IEntity<?>> T parseEntity(Class<T> type, InputStream in) throws SAXException, IOException {
      return parseEntity(type, new InputSource(removeBOM(in)));
   }

   public static <T extends IEntity<?>> T parseEntity(Class<T> type, String xml) throws SAXException, IOException {
      return parseEntity(type, new InputSource(new StringReader(removeBOM(xml))));
   }

   // to remove Byte Order Mark(BOM) at the head of windows utf-8 file
   @SuppressWarnings("unchecked")
   private static <T> T removeBOM(T obj) throws IOException {
      if (obj instanceof String) {
         String str = (String) obj;

         if (str.length() != 0 && str.charAt(0) == 0xFEFF) {
            return (T) str.substring(1);
         } else {
            return obj;
         }
      } else if (obj instanceof InputStream) {
         BufferedInputStream in = new BufferedInputStream((InputStream) obj);

         in.mark(3);

         if (in.read() != 0xEF || in.read() != 0xBB || in.read() != 0xBF) {
            in.reset();
         }

         return (T) in;
      } else if (obj instanceof Reader) {
         BufferedReader in = new BufferedReader((Reader) obj);

         in.mark(1);

         if (in.read() != 0xFEFF) {
            in.reset();
         }

         return (T) in;
      } else {
         return obj;
      }
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

   @Override
   public void characters(char[] ch, int start, int length) throws SAXException {
      m_text.append(ch, start, length);
   }

   @Override
   public void endDocument() throws SAXException {
      m_linker.finish();
   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException {
      if (uri == null || uri.length() == 0) {
         m_objs.pop();
         m_tags.pop();

      }

      m_text.setLength(0);
   }

   private IEntity<?> getEntity() {
      return m_entity;
   }

   protected String getText() {
      return m_text.toString();
   }

   private void parseForDefaultServer(DefaultServer parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      m_objs.push(parentObj);
      m_tags.push(qName);
   }

   private void parseForDomain(Domain parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      if (ENTITY_GROUP.equals(qName)) {
         Group group = m_maker.buildGroup(attributes);

         m_linker.onGroup(parentObj, group);
         m_objs.push(group);
      } else {
         throw new SAXException(String.format("Element(%s) is not expected under domain!", qName));
      }

      m_tags.push(qName);
   }

   private void parseForGroup(Group parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      if (ENTITY_SERVER.equals(qName)) {
         Server server = m_maker.buildServer(attributes);

         m_linker.onServer(parentObj, server);
         m_objs.push(server);
      } else {
         throw new SAXException(String.format("Element(%s) is not expected under group!", qName));
      }

      m_tags.push(qName);
   }

   private void parseForGroupServer(GroupServer parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      m_objs.push(parentObj);
      m_tags.push(qName);
   }

   private void parseForNetwork(Network parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      m_objs.push(parentObj);
      m_tags.push(qName);
   }

   private void parseForNetworkPolicy(NetworkPolicy parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      if (ENTITY_NETWORK.equals(qName)) {
         Network network = m_maker.buildNetwork(attributes);

         m_linker.onNetwork(parentObj, network);
         m_objs.push(network);
      } else {
         throw new SAXException(String.format("Element(%s) is not expected under network-policy!", qName));
      }

      m_tags.push(qName);
   }

   private void parseForRouterConfig(RouterConfig parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      if (ENTITY_DEFAULT_SERVER.equals(qName)) {
         DefaultServer defaultServer = m_maker.buildDefaultServer(attributes);

         m_linker.onDefaultServer(parentObj, defaultServer);
         m_objs.push(defaultServer);
      } else if (ENTITY_NETWORK_POLICY.equals(qName)) {
         NetworkPolicy networkPolicy = m_maker.buildNetworkPolicy(attributes);

         m_linker.onNetworkPolicy(parentObj, networkPolicy);
         m_objs.push(networkPolicy);
      } else if (ENTITY_SERVER_GROUP.equals(qName)) {
         ServerGroup serverGroup = m_maker.buildServerGroup(attributes);

         m_linker.onServerGroup(parentObj, serverGroup);
         m_objs.push(serverGroup);
      } else if (ENTITY_DOMAIN.equals(qName)) {
         Domain domain = m_maker.buildDomain(attributes);

         m_linker.onDomain(parentObj, domain);
         m_objs.push(domain);
      } else {
         throw new SAXException(String.format("Element(%s) is not expected under router-config!", qName));
      }

      m_tags.push(qName);
   }

   private void parseForServer(Server parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      m_objs.push(parentObj);
      m_tags.push(qName);
   }

   private void parseForServerGroup(ServerGroup parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      if (ENTITY_GROUP_SERVER.equals(qName)) {
         GroupServer groupServer = m_maker.buildGroupServer(attributes);

         m_linker.onGroupServer(parentObj, groupServer);
         m_objs.push(groupServer);
      } else {
         throw new SAXException(String.format("Element(%s) is not expected under server-group!", qName));
      }

      m_tags.push(qName);
   }

   private void parseRoot(String qName, Attributes attributes) throws SAXException {
      if (ENTITY_ROUTER_CONFIG.equals(qName)) {
         RouterConfig routerConfig = m_maker.buildRouterConfig(attributes);

         m_entity = routerConfig;
         m_objs.push(routerConfig);
         m_tags.push(qName);
      } else if (ENTITY_DEFAULT_SERVER.equals(qName)) {
         DefaultServer defaultServer = m_maker.buildDefaultServer(attributes);

         m_entity = defaultServer;
         m_objs.push(defaultServer);
         m_tags.push(qName);
      } else if (ENTITY_NETWORK_POLICY.equals(qName)) {
         NetworkPolicy networkPolicy = m_maker.buildNetworkPolicy(attributes);

         m_entity = networkPolicy;
         m_objs.push(networkPolicy);
         m_tags.push(qName);
      } else if (ENTITY_NETWORK.equals(qName)) {
         Network network = m_maker.buildNetwork(attributes);

         m_entity = network;
         m_objs.push(network);
         m_tags.push(qName);
      } else if (ENTITY_SERVER_GROUP.equals(qName)) {
         ServerGroup serverGroup = m_maker.buildServerGroup(attributes);

         m_entity = serverGroup;
         m_objs.push(serverGroup);
         m_tags.push(qName);
      } else if (ENTITY_GROUP_SERVER.equals(qName)) {
         GroupServer groupServer = m_maker.buildGroupServer(attributes);

         m_entity = groupServer;
         m_objs.push(groupServer);
         m_tags.push(qName);
      } else if (ENTITY_DOMAIN.equals(qName)) {
         Domain domain = m_maker.buildDomain(attributes);

         m_entity = domain;
         m_objs.push(domain);
         m_tags.push(qName);
      } else if (ENTITY_GROUP.equals(qName)) {
         Group group = m_maker.buildGroup(attributes);

         m_entity = group;
         m_objs.push(group);
         m_tags.push(qName);
      } else if (ENTITY_SERVER.equals(qName)) {
         Server server = m_maker.buildServer(attributes);

         m_entity = server;
         m_objs.push(server);
         m_tags.push(qName);
      } else {
         throw new SAXException("Unknown root element(" + qName + ") found!");
      }
   }

   @Override
   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if (uri == null || uri.length() == 0) {
         if (m_objs.isEmpty()) { // root
            parseRoot(qName, attributes);
         } else {
            Object parent = m_objs.peek();
            String tag = m_tags.peek();

            if (parent instanceof RouterConfig) {
               parseForRouterConfig((RouterConfig) parent, tag, qName, attributes);
            } else if (parent instanceof DefaultServer) {
               parseForDefaultServer((DefaultServer) parent, tag, qName, attributes);
            } else if (parent instanceof NetworkPolicy) {
               parseForNetworkPolicy((NetworkPolicy) parent, tag, qName, attributes);
            } else if (parent instanceof Network) {
               parseForNetwork((Network) parent, tag, qName, attributes);
            } else if (parent instanceof ServerGroup) {
               parseForServerGroup((ServerGroup) parent, tag, qName, attributes);
            } else if (parent instanceof GroupServer) {
               parseForGroupServer((GroupServer) parent, tag, qName, attributes);
            } else if (parent instanceof Domain) {
               parseForDomain((Domain) parent, tag, qName, attributes);
            } else if (parent instanceof Group) {
               parseForGroup((Group) parent, tag, qName, attributes);
            } else if (parent instanceof Server) {
               parseForServer((Server) parent, tag, qName, attributes);
            } else {
               throw new RuntimeException(String.format("Unknown entity(%s) under %s!", qName, parent.getClass().getName()));
            }
         }

         m_text.setLength(0);
        } else {
         throw new SAXException(String.format("Namespace(%s) is not supported by %s.", uri, this.getClass().getName()));
      }
   }

   protected java.util.Date toDate(String str, String format) {
      try {
         return new java.text.SimpleDateFormat(format).parse(str);
      } catch (java.text.ParseException e) {
         throw new RuntimeException(String.format("Unable to parse date(%s) in format(%s)!", str, format), e);
      }
   }
}
