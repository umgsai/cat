package com.dianping.cat.configuration.business.transform;

import static com.dianping.cat.configuration.business.Constants.ATTR_ALARM;
import static com.dianping.cat.configuration.business.Constants.ATTR_ID;
import static com.dianping.cat.configuration.business.Constants.ATTR_PRIVILEGE;
import static com.dianping.cat.configuration.business.Constants.ATTR_SHOW_AVG;
import static com.dianping.cat.configuration.business.Constants.ATTR_SHOW_COUNT;
import static com.dianping.cat.configuration.business.Constants.ATTR_SHOW_SUM;
import static com.dianping.cat.configuration.business.Constants.ATTR_TITLE;
import static com.dianping.cat.configuration.business.Constants.ATTR_VIEW_ORDER;
import static com.dianping.cat.configuration.business.Constants.ELEMENT_PATTERN;
import static com.dianping.cat.configuration.business.Constants.ENTITY_BUSINESS_ITEM_CONFIG;
import static com.dianping.cat.configuration.business.Constants.ENTITY_BUSINESS_REPORT_CONFIG;
import static com.dianping.cat.configuration.business.Constants.ENTITY_CUSTOM_CONFIG;

import java.lang.reflect.Array;
import java.util.Collection;

import com.dianping.cat.configuration.business.IEntity;
import com.dianping.cat.configuration.business.IVisitor;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;

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

   protected void tagWithText(String name, String text, Object... nameValues) {
      if (text == null) {
         return;
      }
      
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

      m_sb.append(">");
      m_sb.append(escape(text, true));
      m_sb.append("</").append(name).append(">\r\n");
   }

   protected void element(String name, String text, String defaultValue, boolean escape) {
      if (text == null || text.equals(defaultValue)) {
         return;
      }
      
      indent();
      
      m_sb.append('<').append(name).append(">");
      
      if (escape) {
         m_sb.append(escape(text, true));
      } else {
         m_sb.append("<![CDATA[").append(text).append("]]>");
      }
      
      m_sb.append("</").append(name).append(">\r\n");
   }

   @Override
   public void visitBusinessItemConfig(BusinessItemConfig businessItemConfig) {
      startTag(ENTITY_BUSINESS_ITEM_CONFIG, true, null, ATTR_ID, businessItemConfig.getId(), ATTR_VIEW_ORDER, businessItemConfig.getViewOrder(), ATTR_TITLE, businessItemConfig.getTitle(), ATTR_SHOW_COUNT, businessItemConfig.isShowCount(), ATTR_SHOW_AVG, businessItemConfig.isShowAvg(), ATTR_SHOW_SUM, businessItemConfig.isShowSum(), ATTR_ALARM, businessItemConfig.isAlarm(), ATTR_PRIVILEGE, businessItemConfig.isPrivilege());
   }

   @Override
   public void visitBusinessReportConfig(BusinessReportConfig businessReportConfig) {
      startTag(ENTITY_BUSINESS_REPORT_CONFIG, null, ATTR_ID, businessReportConfig.getId());

      if (!businessReportConfig.getBusinessItemConfigs().isEmpty()) {
         for (BusinessItemConfig businessItemConfig : businessReportConfig.getBusinessItemConfigs().values()) {
            businessItemConfig.accept(m_visitor);
         }
      }

      if (!businessReportConfig.getCustomConfigs().isEmpty()) {
         for (CustomConfig customConfig : businessReportConfig.getCustomConfigs().values()) {
            customConfig.accept(m_visitor);
         }
      }

      endTag(ENTITY_BUSINESS_REPORT_CONFIG);
   }

   @Override
   public void visitCustomConfig(CustomConfig customConfig) {
      startTag(ENTITY_CUSTOM_CONFIG, null, ATTR_ID, customConfig.getId(), ATTR_VIEW_ORDER, customConfig.getViewOrder(), ATTR_TITLE, customConfig.getTitle(), ATTR_ALARM, customConfig.isAlarm(), ATTR_PRIVILEGE, customConfig.isPrivilege());

      element(ELEMENT_PATTERN, customConfig.getPattern(), null,  true);

      endTag(ENTITY_CUSTOM_CONFIG);
   }
}
