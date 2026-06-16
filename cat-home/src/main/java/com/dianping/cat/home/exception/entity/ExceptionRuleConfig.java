package com.dianping.cat.home.exception.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.exception.BaseEntity;
import com.dianping.cat.home.exception.IVisitor;

public class ExceptionRuleConfig extends BaseEntity<ExceptionRuleConfig> {
   private Map<String, ExceptionLimit> m_exceptionLimits = new LinkedHashMap<String, ExceptionLimit>();

   private Map<String, ExceptionExclude> m_exceptionExcludes = new LinkedHashMap<String, ExceptionExclude>();

   public ExceptionRuleConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitExceptionRuleConfig(this);
   }

   public ExceptionRuleConfig addExceptionExclude(ExceptionExclude exceptionExclude) {
      m_exceptionExcludes.put(exceptionExclude.getId(), exceptionExclude);
      return this;
   }

   public ExceptionRuleConfig addExceptionLimit(ExceptionLimit exceptionLimit) {
      m_exceptionLimits.put(exceptionLimit.getId(), exceptionLimit);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ExceptionRuleConfig) {
         ExceptionRuleConfig _o = (ExceptionRuleConfig) obj;

         if (!equals(getExceptionLimits(), _o.getExceptionLimits())) {
            return false;
         }

         if (!equals(getExceptionExcludes(), _o.getExceptionExcludes())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public ExceptionExclude findExceptionExclude(String id) {
      return m_exceptionExcludes.get(id);
   }

   public ExceptionLimit findExceptionLimit(String id) {
      return m_exceptionLimits.get(id);
   }

   public ExceptionExclude findOrCreateExceptionExclude(String id) {
      ExceptionExclude exceptionExclude = m_exceptionExcludes.get(id);

      if (exceptionExclude == null) {
         synchronized (m_exceptionExcludes) {
            exceptionExclude = m_exceptionExcludes.get(id);

            if (exceptionExclude == null) {
               exceptionExclude = new ExceptionExclude(id);
               m_exceptionExcludes.put(id, exceptionExclude);
            }
         }
      }

      return exceptionExclude;
   }

   public ExceptionLimit findOrCreateExceptionLimit(String id) {
      ExceptionLimit exceptionLimit = m_exceptionLimits.get(id);

      if (exceptionLimit == null) {
         synchronized (m_exceptionLimits) {
            exceptionLimit = m_exceptionLimits.get(id);

            if (exceptionLimit == null) {
               exceptionLimit = new ExceptionLimit(id);
               m_exceptionLimits.put(id, exceptionLimit);
            }
         }
      }

      return exceptionLimit;
   }

   public Map<String, ExceptionExclude> getExceptionExcludes() {
      return m_exceptionExcludes;
   }

   public Map<String, ExceptionLimit> getExceptionLimits() {
      return m_exceptionLimits;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_exceptionLimits == null ? 0 : m_exceptionLimits.hashCode());
      hash = hash * 31 + (m_exceptionExcludes == null ? 0 : m_exceptionExcludes.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(ExceptionRuleConfig other) {
   }

   public ExceptionExclude removeExceptionExclude(String id) {
      return m_exceptionExcludes.remove(id);
   }

   public ExceptionLimit removeExceptionLimit(String id) {
      return m_exceptionLimits.remove(id);
   }

}
