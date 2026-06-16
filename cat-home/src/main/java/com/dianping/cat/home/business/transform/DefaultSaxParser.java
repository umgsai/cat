package com.dianping.cat.home.business.transform;

import static com.dianping.cat.home.business.Constants.ENTITY_BUSINESS_ITEM;
import static com.dianping.cat.home.business.Constants.ENTITY_BUSINESS_TAG_CONFIG;
import static com.dianping.cat.home.business.Constants.ENTITY_TAG;

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

import com.dianping.cat.home.business.IEntity;
import com.dianping.cat.home.business.entity.BusinessItem;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.home.business.entity.Tag;

public class DefaultSaxParser extends DefaultHandler {

   private DefaultLinker m_linker = new DefaultLinker(true);

   private DefaultSaxMaker m_maker = new DefaultSaxMaker();

   private Stack<String> m_tags = new Stack<String>();

   private Stack<Object> m_objs = new Stack<Object>();

   private IEntity<?> m_entity;

   private StringBuilder m_text = new StringBuilder();

   public static BusinessTagConfig parse(InputStream in) throws SAXException, IOException {
      return parseEntity(BusinessTagConfig.class, new InputSource(removeBOM(in)));
   }

   public static BusinessTagConfig parse(Reader reader) throws SAXException, IOException {
      return parseEntity(BusinessTagConfig.class, new InputSource(removeBOM(reader)));
   }

   public static BusinessTagConfig parse(String xml) throws SAXException, IOException {
      return parseEntity(BusinessTagConfig.class, new InputSource(new StringReader(removeBOM(xml))));
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

   private void parseForBusinessItem(BusinessItem parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      m_objs.push(parentObj);
      m_tags.push(qName);
   }

   private void parseForBusinessTagConfig(BusinessTagConfig parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      if (ENTITY_TAG.equals(qName)) {
         Tag tag = m_maker.buildTag(attributes);

         m_linker.onTag(parentObj, tag);
         m_objs.push(tag);
      } else {
         throw new SAXException(String.format("Element(%s) is not expected under business-tag-config!", qName));
      }

      m_tags.push(qName);
   }

   private void parseForTag(Tag parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      if (ENTITY_BUSINESS_ITEM.equals(qName)) {
         BusinessItem businessItem = m_maker.buildBusinessItem(attributes);

         m_linker.onBusinessItem(parentObj, businessItem);
         m_objs.push(businessItem);
      } else {
         throw new SAXException(String.format("Element(%s) is not expected under tag!", qName));
      }

      m_tags.push(qName);
   }

   private void parseRoot(String qName, Attributes attributes) throws SAXException {
      if (ENTITY_BUSINESS_TAG_CONFIG.equals(qName)) {
         BusinessTagConfig businessTagConfig = m_maker.buildBusinessTagConfig(attributes);

         m_entity = businessTagConfig;
         m_objs.push(businessTagConfig);
         m_tags.push(qName);
      } else if (ENTITY_TAG.equals(qName)) {
         Tag tag = m_maker.buildTag(attributes);

         m_entity = tag;
         m_objs.push(tag);
         m_tags.push(qName);
      } else if (ENTITY_BUSINESS_ITEM.equals(qName)) {
         BusinessItem businessItem = m_maker.buildBusinessItem(attributes);

         m_entity = businessItem;
         m_objs.push(businessItem);
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

            if (parent instanceof BusinessTagConfig) {
               parseForBusinessTagConfig((BusinessTagConfig) parent, tag, qName, attributes);
            } else if (parent instanceof Tag) {
               parseForTag((Tag) parent, tag, qName, attributes);
            } else if (parent instanceof BusinessItem) {
               parseForBusinessItem((BusinessItem) parent, tag, qName, attributes);
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
