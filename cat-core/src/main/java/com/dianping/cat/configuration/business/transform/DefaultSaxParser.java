package com.dianping.cat.configuration.business.transform;

import static com.dianping.cat.configuration.business.Constants.ELEMENT_PATTERN;

import static com.dianping.cat.configuration.business.Constants.ENTITY_BUSINESS_ITEM_CONFIG;
import static com.dianping.cat.configuration.business.Constants.ENTITY_BUSINESS_REPORT_CONFIG;
import static com.dianping.cat.configuration.business.Constants.ENTITY_CUSTOM_CONFIG;

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

import com.dianping.cat.configuration.business.IEntity;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;

public class DefaultSaxParser extends DefaultHandler {

   private DefaultLinker m_linker = new DefaultLinker(true);

   private DefaultSaxMaker m_maker = new DefaultSaxMaker();

   private Stack<String> m_tags = new Stack<String>();

   private Stack<Object> m_objs = new Stack<Object>();

   private IEntity<?> m_entity;

   private StringBuilder m_text = new StringBuilder();

   public static BusinessReportConfig parse(InputStream in) throws SAXException, IOException {
      return parseEntity(BusinessReportConfig.class, new InputSource(removeBOM(in)));
   }

   public static BusinessReportConfig parse(Reader reader) throws SAXException, IOException {
      return parseEntity(BusinessReportConfig.class, new InputSource(removeBOM(reader)));
   }

   public static BusinessReportConfig parse(String xml) throws SAXException, IOException {
      return parseEntity(BusinessReportConfig.class, new InputSource(new StringReader(removeBOM(xml))));
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

         if (currentObj instanceof CustomConfig) {
            CustomConfig customConfig = (CustomConfig) currentObj;

            if (ELEMENT_PATTERN.equals(currentTag)) {
               customConfig.setPattern(getText());
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

   private void parseForBusinessItemConfig(BusinessItemConfig parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      m_objs.push(parentObj);
      m_tags.push(qName);
   }

   private void parseForBusinessReportConfig(BusinessReportConfig parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      if (ENTITY_BUSINESS_ITEM_CONFIG.equals(qName)) {
         BusinessItemConfig businessItemConfig = m_maker.buildBusinessItemConfig(attributes);

         m_linker.onBusinessItemConfig(parentObj, businessItemConfig);
         m_objs.push(businessItemConfig);
      } else if (ENTITY_CUSTOM_CONFIG.equals(qName)) {
         CustomConfig customConfig = m_maker.buildCustomConfig(attributes);

         m_linker.onCustomConfig(parentObj, customConfig);
         m_objs.push(customConfig);
      } else {
         throw new SAXException(String.format("Element(%s) is not expected under business-report-config!", qName));
      }

      m_tags.push(qName);
   }

   private void parseForCustomConfig(CustomConfig parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
      if (ELEMENT_PATTERN.equals(qName)) {
         m_objs.push(parentObj);
      } else {
         throw new SAXException(String.format("Element(%s) is not expected under custom-config!", qName));
      }

      m_tags.push(qName);
   }

   private void parseRoot(String qName, Attributes attributes) throws SAXException {
      if (ENTITY_BUSINESS_REPORT_CONFIG.equals(qName)) {
         BusinessReportConfig businessReportConfig = m_maker.buildBusinessReportConfig(attributes);

         m_entity = businessReportConfig;
         m_objs.push(businessReportConfig);
         m_tags.push(qName);
      } else if (ENTITY_BUSINESS_ITEM_CONFIG.equals(qName)) {
         BusinessItemConfig businessItemConfig = m_maker.buildBusinessItemConfig(attributes);

         m_entity = businessItemConfig;
         m_objs.push(businessItemConfig);
         m_tags.push(qName);
      } else if (ENTITY_CUSTOM_CONFIG.equals(qName)) {
         CustomConfig customConfig = m_maker.buildCustomConfig(attributes);

         m_entity = customConfig;
         m_objs.push(customConfig);
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

            if (parent instanceof BusinessReportConfig) {
               parseForBusinessReportConfig((BusinessReportConfig) parent, tag, qName, attributes);
            } else if (parent instanceof BusinessItemConfig) {
               parseForBusinessItemConfig((BusinessItemConfig) parent, tag, qName, attributes);
            } else if (parent instanceof CustomConfig) {
               parseForCustomConfig((CustomConfig) parent, tag, qName, attributes);
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
