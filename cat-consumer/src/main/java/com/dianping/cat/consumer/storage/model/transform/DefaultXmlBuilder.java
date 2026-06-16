package com.dianping.cat.consumer.storage.model.transform;

import static com.dianping.cat.consumer.storage.model.Constants.ATTR_AVG;
import static com.dianping.cat.consumer.storage.model.Constants.ATTR_COUNT;
import static com.dianping.cat.consumer.storage.model.Constants.ATTR_ENDTIME;
import static com.dianping.cat.consumer.storage.model.Constants.ATTR_ERROR;
import static com.dianping.cat.consumer.storage.model.Constants.ATTR_ID;
import static com.dianping.cat.consumer.storage.model.Constants.ATTR_LONG_COUNT;
import static com.dianping.cat.consumer.storage.model.Constants.ATTR_NAME;
import static com.dianping.cat.consumer.storage.model.Constants.ATTR_STARTTIME;
import static com.dianping.cat.consumer.storage.model.Constants.ATTR_STATEMENT;
import static com.dianping.cat.consumer.storage.model.Constants.ATTR_SUM;
import static com.dianping.cat.consumer.storage.model.Constants.ATTR_TYPE;
import static com.dianping.cat.consumer.storage.model.Constants.ELEMENT_ID;
import static com.dianping.cat.consumer.storage.model.Constants.ELEMENT_IP;
import static com.dianping.cat.consumer.storage.model.Constants.ELEMENT_OP;
import static com.dianping.cat.consumer.storage.model.Constants.ENTITY_DOMAIN;
import static com.dianping.cat.consumer.storage.model.Constants.ENTITY_MACHINE;
import static com.dianping.cat.consumer.storage.model.Constants.ENTITY_OPERATION;
import static com.dianping.cat.consumer.storage.model.Constants.ENTITY_SEGMENT;
import static com.dianping.cat.consumer.storage.model.Constants.ENTITY_SQL;
import static com.dianping.cat.consumer.storage.model.Constants.ENTITY_STORAGE_REPORT;

import java.lang.reflect.Array;
import java.util.Collection;

import com.dianping.cat.consumer.storage.model.IEntity;
import com.dianping.cat.consumer.storage.model.IVisitor;
import com.dianping.cat.consumer.storage.model.entity.Domain;
import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.entity.Segment;
import com.dianping.cat.consumer.storage.model.entity.Sql;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;

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

   protected String toString(java.util.Date date, String format) {
      if (date != null) {
         return new java.text.SimpleDateFormat(format).format(date);
      } else {
         return null;
      }
   }

   protected String toString(Number number, String format) {
      if (number != null) {
         return new java.text.DecimalFormat(format).format(number);
      } else {
         return null;
      }
   }

   @Override
   public void visitDomain(Domain domain) {
      startTag(ENTITY_DOMAIN, null, ATTR_ID, domain.getId());

      if (!domain.getOperations().isEmpty()) {
         for (Operation operation : domain.getOperations().values()) {
            operation.accept(m_visitor);
         }
      }

      if (!domain.getSqls().isEmpty()) {
         for (Sql sql : domain.getSqls().values()) {
            sql.accept(m_visitor);
         }
      }

      endTag(ENTITY_DOMAIN);
   }

   @Override
   public void visitMachine(Machine machine) {
      startTag(ENTITY_MACHINE, null, ATTR_ID, machine.getId());

      if (!machine.getDomains().isEmpty()) {
         for (Domain domain : machine.getDomains().values()) {
            domain.accept(m_visitor);
         }
      }

      endTag(ENTITY_MACHINE);
   }

   @Override
   public void visitOperation(Operation operation) {
      startTag(ENTITY_OPERATION, null, ATTR_ID, operation.getId(), ATTR_COUNT, operation.getCount(), ATTR_AVG, toString(operation.getAvg(), "0.0"), ATTR_SUM, toString(operation.getSum(), "0.0"), ATTR_ERROR, operation.getError(), ATTR_LONG_COUNT, operation.getLongCount());

      if (!operation.getSegments().isEmpty()) {
         for (Segment segment : operation.getSegments().values()) {
            segment.accept(m_visitor);
         }
      }

      endTag(ENTITY_OPERATION);
   }

   @Override
   public void visitSegment(Segment segment) {
      startTag(ENTITY_SEGMENT, true, null, ATTR_ID, segment.getId(), ATTR_COUNT, segment.getCount(), ATTR_AVG, toString(segment.getAvg(), "0.0"), ATTR_SUM, toString(segment.getSum(), "0.0"), ATTR_ERROR, segment.getError(), ATTR_LONG_COUNT, segment.getLongCount());
   }

   @Override
   public void visitSql(Sql sql) {
      startTag(ENTITY_SQL, true, null, ATTR_ID, sql.getId(), ATTR_STATEMENT, sql.getStatement(), ATTR_COUNT, sql.getCount());
   }

   @Override
   public void visitStorageReport(StorageReport storageReport) {
      startTag(ENTITY_STORAGE_REPORT, null, ATTR_ID, storageReport.getId(), ATTR_NAME, storageReport.getName(), ATTR_TYPE, storageReport.getType(), ATTR_STARTTIME, toString(storageReport.getStartTime(), "yyyy-MM-dd HH:mm:ss"), ATTR_ENDTIME, toString(storageReport.getEndTime(), "yyyy-MM-dd HH:mm:ss"));

      if (!storageReport.getIds().isEmpty()) {
         for (String id : storageReport.getIds()) {
            tagWithText(ELEMENT_ID, id);
         }
      }

      if (!storageReport.getIps().isEmpty()) {
         for (String ip : storageReport.getIps()) {
            tagWithText(ELEMENT_IP, ip);
         }
      }

      if (!storageReport.getOps().isEmpty()) {
         for (String op : storageReport.getOps()) {
            tagWithText(ELEMENT_OP, op);
         }
      }

      if (!storageReport.getMachines().isEmpty()) {
         for (Machine machine : storageReport.getMachines().values()) {
            machine.accept(m_visitor);
         }
      }

      endTag(ENTITY_STORAGE_REPORT);
   }
}
