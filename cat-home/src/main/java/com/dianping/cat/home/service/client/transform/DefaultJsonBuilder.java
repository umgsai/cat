package com.dianping.cat.home.service.client.transform;

import static com.dianping.cat.home.service.client.Constants.ATTR_AVG;
import static com.dianping.cat.home.service.client.Constants.ATTR_DOMAIN;
import static com.dianping.cat.home.service.client.Constants.ATTR_END_TIME;
import static com.dianping.cat.home.service.client.Constants.ATTR_FAILURECOUNT;
import static com.dianping.cat.home.service.client.Constants.ATTR_FAILUREPERCENT;
import static com.dianping.cat.home.service.client.Constants.ATTR_ID;
import static com.dianping.cat.home.service.client.Constants.ATTR_QPS;
import static com.dianping.cat.home.service.client.Constants.ATTR_SERVICE;
import static com.dianping.cat.home.service.client.Constants.ATTR_START_TIME;
import static com.dianping.cat.home.service.client.Constants.ATTR_SUM;
import static com.dianping.cat.home.service.client.Constants.ATTR_TIMEOUT;
import static com.dianping.cat.home.service.client.Constants.ATTR_TOTALCOUNT;
import static com.dianping.cat.home.service.client.Constants.ENTITY_DOMAINS;
import static com.dianping.cat.home.service.client.Constants.ENTITY_METHODS;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import com.dianping.cat.home.service.client.IEntity;
import com.dianping.cat.home.service.client.IVisitor;
import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.entity.Domain;
import com.dianping.cat.home.service.client.entity.Method;

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
   public void visitClientReport(ClientReport clientReport) {
      attributes(null, ATTR_DOMAIN, clientReport.getDomain(), ATTR_START_TIME, toString(clientReport.getStartTime(), "yyyy-MM-dd HH:mm:ss"), ATTR_END_TIME, toString(clientReport.getEndTime(), "yyyy-MM-dd HH:mm:ss"));

      if (!clientReport.getDomains().isEmpty()) {
         objectBegin(ENTITY_DOMAINS);

         for (Map.Entry<String, Domain> e : clientReport.getDomains().entrySet()) {
            String key = String.valueOf(e.getKey());

            objectBegin(key);
            e.getValue().accept(m_visitor);
            objectEnd(key);
         }

         objectEnd(ENTITY_DOMAINS);
      }
   }

   @Override
   public void visitDomain(Domain domain) {
      attributes(null, ATTR_ID, domain.getId(), ATTR_TOTALCOUNT, domain.getTotalCount(), ATTR_FAILURECOUNT, domain.getFailureCount(), ATTR_FAILUREPERCENT, domain.getFailurePercent(), ATTR_SUM, domain.getSum(), ATTR_AVG, domain.getAvg());

      if (!domain.getMethods().isEmpty()) {
         objectBegin(ENTITY_METHODS);

         for (Map.Entry<String, Method> e : domain.getMethods().entrySet()) {
            String key = String.valueOf(e.getKey());

            objectBegin(key);
            e.getValue().accept(m_visitor);
            objectEnd(key);
         }

         objectEnd(ENTITY_METHODS);
      }
   }

   @Override
   public void visitMethod(Method method) {
      attributes(null, ATTR_ID, method.getId(), ATTR_SERVICE, method.getService(), ATTR_TOTALCOUNT, method.getTotalCount(), ATTR_FAILURECOUNT, method.getFailureCount(), ATTR_FAILUREPERCENT, method.getFailurePercent(), ATTR_SUM, method.getSum(), ATTR_AVG, method.getAvg(), ATTR_QPS, method.getQps(), ATTR_TIMEOUT, method.getTimeout());
   }
}
