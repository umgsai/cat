package com.dianping.cat.configuration.server.filter.transform;

import static com.dianping.cat.configuration.server.filter.Constants.ELEMENT_DOMAIN;
import static com.dianping.cat.configuration.server.filter.Constants.ELEMENT_DOMAINS;
import static com.dianping.cat.configuration.server.filter.Constants.ELEMENT_TRANSACTION_NAME;
import static com.dianping.cat.configuration.server.filter.Constants.ELEMENT_TRANSACTION_NAMES;
import static com.dianping.cat.configuration.server.filter.Constants.ELEMENT_TRANSACTION_TYPE;
import static com.dianping.cat.configuration.server.filter.Constants.ELEMENT_TRANSACTION_TYPES;

import static com.dianping.cat.configuration.server.filter.Constants.ENTITY_ATOMIC_TREE_CONFIG;
import static com.dianping.cat.configuration.server.filter.Constants.ENTITY_CRASH_LOG_DOMAIN;
import static com.dianping.cat.configuration.server.filter.Constants.ENTITY_SERVER_FILTER_CONFIG;

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

import com.dianping.cat.configuration.server.filter.IEntity;
import com.dianping.cat.configuration.server.filter.entity.AtomicTreeConfig;
import com.dianping.cat.configuration.server.filter.entity.CrashLogDomain;
import com.dianping.cat.configuration.server.filter.entity.ServerFilterConfig;

public class DefaultSaxParser extends DefaultHandler {

   private DefaultLinker m_linker = new DefaultLinker(true);

   private DefaultSaxMaker m_maker = new DefaultSaxMaker();

   private Stack<String> m_tags = new Stack<String>();

   private Stack<Object> m_objs = new Stack<Object>();

   private IEntity<?> m_entity;

   private StringBuilder m_text = new StringBuilder();

   public static ServerFilterConfig parse(InputStream in) throws SAXException, IOException {
      return parseEntity(ServerFilterConfig.class, new InputSource(removeBOM(in)));
   }

   public static ServerFilterConfig parse(Reader reader) throws SAXException, IOException {
      return parseEntity(ServerFilterConfig.class, new InputSource(removeBOM(reader)));
   }

   public static ServerFilterConfig parse(String xml) throws SAXException, IOException {
      return parseEntity(ServerFilterConfig.class, new InputSource(new StringReader(removeBOM(xml))));
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
         Object currentObj = m_objs.pop();
         String currentTag = m_tags.pop();

         if (currentObj instanceof ServerFilterConfig) {
            ServerFilterConfig serverFilterConfig = (ServerFilterConfig) currentObj;

            if (ELEMENT_TRANSACTION_TYPE.equals(currentTag)) {
               serverFilterConfig.addTransactionType(getText());
            } else if (ELEMENT_TRANSACTION_NAME.equals(currentTag)) {
               serverFilterConfig.addTransactionName(getText());
            } else if (ELEMENT_DOMAIN.equals(currentTag)) {
               serverFilterConfig.addDomain(getText());
            }
         }
      }

      m_text.setLength(0);
   }

   private IEntity<?> getEntity() {
      return m_entity;
   }

   protected String getText() {
      return m_text.toString();
   }

   private void parseForAtomicTreeConfig(AtomicTreeConfig parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      m_objs.push(parentObj);
      m_tags.push(qName);
   }

   private void parseForCrashLogDomain(CrashLogDomain parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      m_objs.push(parentObj);
      m_tags.push(qName);
   }

   private void parseForServerFilterConfig(ServerFilterConfig parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      if (ELEMENT_TRANSACTION_TYPES.equals(qName) || ELEMENT_TRANSACTION_TYPE.equals(qName) || ELEMENT_TRANSACTION_NAMES.equals(qName) || ELEMENT_TRANSACTION_NAME.equals(qName) || ELEMENT_DOMAINS.equals(qName) || ELEMENT_DOMAIN.equals(qName)) {
         m_objs.push(parentObj);
      } else if (ENTITY_CRASH_LOG_DOMAIN.equals(qName)) {
         CrashLogDomain crashLogDomain = m_maker.buildCrashLogDomain(attributes);

         m_linker.onCrashLogDomain(parentObj, crashLogDomain);
         m_objs.push(crashLogDomain);
      } else if (ENTITY_ATOMIC_TREE_CONFIG.equals(qName)) {
         AtomicTreeConfig atomicTreeConfig = m_maker.buildAtomicTreeConfig(attributes);

         m_linker.onAtomicTreeConfig(parentObj, atomicTreeConfig);
         m_objs.push(atomicTreeConfig);
      } else {
         throw new SAXException(String.format("Element(%s) is not expected under server-filter-config!", qName));
      }

      m_tags.push(qName);
   }

   private void parseRoot(String qName, Attributes attributes) throws SAXException {
      if (ENTITY_SERVER_FILTER_CONFIG.equals(qName)) {
         ServerFilterConfig serverFilterConfig = m_maker.buildServerFilterConfig(attributes);

         m_entity = serverFilterConfig;
         m_objs.push(serverFilterConfig);
         m_tags.push(qName);
      } else if (ENTITY_CRASH_LOG_DOMAIN.equals(qName)) {
         CrashLogDomain crashLogDomain = m_maker.buildCrashLogDomain(attributes);

         m_entity = crashLogDomain;
         m_objs.push(crashLogDomain);
         m_tags.push(qName);
      } else if (ENTITY_ATOMIC_TREE_CONFIG.equals(qName)) {
         AtomicTreeConfig atomicTreeConfig = m_maker.buildAtomicTreeConfig(attributes);

         m_entity = atomicTreeConfig;
         m_objs.push(atomicTreeConfig);
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

            if (parent instanceof ServerFilterConfig) {
               parseForServerFilterConfig((ServerFilterConfig) parent, tag, qName, attributes);
            } else if (parent instanceof CrashLogDomain) {
               parseForCrashLogDomain((CrashLogDomain) parent, tag, qName, attributes);
            } else if (parent instanceof AtomicTreeConfig) {
               parseForAtomicTreeConfig((AtomicTreeConfig) parent, tag, qName, attributes);
            } else {
               throw new RuntimeException(String.format("Unknown entity(%s) under %s!", qName, parent.getClass().getName()));
            }
         }

         m_text.setLength(0);
        } else {
         throw new SAXException(String.format("Namespace(%s) is not supported by %s.", uri, this.getClass().getName()));
      }
   }
}
