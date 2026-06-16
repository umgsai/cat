package com.dianping.cat.home.service.client.entity;

import static com.dianping.cat.home.service.client.Constants.ATTR_ID;
import static com.dianping.cat.home.service.client.Constants.ENTITY_DOMAIN;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.service.client.BaseEntity;
import com.dianping.cat.home.service.client.IVisitor;

public class Domain extends BaseEntity<Domain> {
   private String m_id;

   private long m_totalCount;

   private long m_failureCount;

   private double m_failurePercent;

   private double m_sum;

   private double m_avg;

   private Map<String, Method> m_methods = new LinkedHashMap<String, Method>();

   public Domain() {
   }

   public Domain(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitDomain(this);
   }

   public Domain addMethod(Method method) {
      m_methods.put(method.getId(), method);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Domain) {
         Domain _o = (Domain) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public Method findMethod(String id) {
      return m_methods.get(id);
   }

   public Method findOrCreateMethod(String id) {
      Method method = m_methods.get(id);

      if (method == null) {
         synchronized (m_methods) {
            method = m_methods.get(id);

            if (method == null) {
               method = new Method(id);
               m_methods.put(id, method);
            }
         }
      }

      return method;
   }

   public double getAvg() {
      return m_avg;
   }

   public long getFailureCount() {
      return m_failureCount;
   }

   public double getFailurePercent() {
      return m_failurePercent;
   }

   public String getId() {
      return m_id;
   }

   public Map<String, Method> getMethods() {
      return m_methods;
   }

   public double getSum() {
      return m_sum;
   }

   public long getTotalCount() {
      return m_totalCount;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   public Domain incFailureCount() {
      m_failureCount++;
      return this;
   }

   public Domain incFailureCount(long failureCount) {
      m_failureCount += failureCount;
      return this;
   }

   public Domain incSum() {
      m_sum++;
      return this;
   }

   public Domain incSum(double sum) {
      m_sum += sum;
      return this;
   }

   public Domain incTotalCount() {
      m_totalCount++;
      return this;
   }

   public Domain incTotalCount(long totalCount) {
      m_totalCount += totalCount;
      return this;
   }

   @Override
   public void mergeAttributes(Domain other) {
      assertAttributeEquals(other, ENTITY_DOMAIN, ATTR_ID, m_id, other.getId());

      m_totalCount = other.getTotalCount();

      m_failureCount = other.getFailureCount();

      m_failurePercent = other.getFailurePercent();

      m_sum = other.getSum();

      m_avg = other.getAvg();
   }

   public Method removeMethod(String id) {
      return m_methods.remove(id);
   }

   public Domain setAvg(double avg) {
      m_avg = avg;
      return this;
   }

   public Domain setFailureCount(long failureCount) {
      m_failureCount = failureCount;
      return this;
   }

   public Domain setFailurePercent(double failurePercent) {
      m_failurePercent = failurePercent;
      return this;
   }

   public Domain setId(String id) {
      m_id = id;
      return this;
   }

   public Domain setSum(double sum) {
      m_sum = sum;
      return this;
   }

   public Domain setTotalCount(long totalCount) {
      m_totalCount = totalCount;
      return this;
   }

}
