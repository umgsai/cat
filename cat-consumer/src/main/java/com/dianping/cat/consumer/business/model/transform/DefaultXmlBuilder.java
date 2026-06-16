package com.dianping.cat.consumer.business.model.transform;

import static com.dianping.cat.consumer.business.model.Constants.ATTR_AVG;
import static com.dianping.cat.consumer.business.model.Constants.ATTR_COUNT;
import static com.dianping.cat.consumer.business.model.Constants.ATTR_DOMAIN;
import static com.dianping.cat.consumer.business.model.Constants.ATTR_ENDTIME;
import static com.dianping.cat.consumer.business.model.Constants.ATTR_ID;
import static com.dianping.cat.consumer.business.model.Constants.ATTR_STARTTIME;
import static com.dianping.cat.consumer.business.model.Constants.ATTR_SUM;
import static com.dianping.cat.consumer.business.model.Constants.ATTR_TYPE;
import static com.dianping.cat.consumer.business.model.Constants.ENTITY_BUSINESS_ITEM;
import static com.dianping.cat.consumer.business.model.Constants.ENTITY_BUSINESS_REPORT;
import static com.dianping.cat.consumer.business.model.Constants.ENTITY_SEGMENT;

import java.lang.reflect.Array;
import java.util.Collection;

import com.dianping.cat.consumer.business.model.IEntity;
import com.dianping.cat.consumer.business.model.IVisitor;
import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;

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
   public void visitBusinessItem(BusinessItem businessItem) {
      startTag(ENTITY_BUSINESS_ITEM, null, ATTR_ID, businessItem.getId(), ATTR_TYPE, businessItem.getType());

      if (!businessItem.getSegments().isEmpty()) {
         for (Segment segment : businessItem.getSegments().values()) {
            segment.accept(m_visitor);
         }
      }

      endTag(ENTITY_BUSINESS_ITEM);
   }

   @Override
   public void visitBusinessReport(BusinessReport businessReport) {
      startTag(ENTITY_BUSINESS_REPORT, null, ATTR_DOMAIN, businessReport.getDomain(), ATTR_STARTTIME, toString(businessReport.getStartTime(), "yyyy-MM-dd HH:mm:ss"), ATTR_ENDTIME, toString(businessReport.getEndTime(), "yyyy-MM-dd HH:mm:ss"));

      if (!businessReport.getBusinessItems().isEmpty()) {
         for (BusinessItem businessItem : businessReport.getBusinessItems().values()) {
            businessItem.accept(m_visitor);
         }
      }

      endTag(ENTITY_BUSINESS_REPORT);
   }

   @Override
   public void visitSegment(Segment segment) {
      startTag(ENTITY_SEGMENT, true, null, ATTR_ID, segment.getId(), ATTR_COUNT, segment.getCount(), ATTR_SUM, segment.getSum(), ATTR_AVG, segment.getAvg());
   }
}
