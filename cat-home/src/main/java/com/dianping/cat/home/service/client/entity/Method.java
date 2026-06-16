package com.dianping.cat.home.service.client.entity;

import static com.dianping.cat.home.service.client.Constants.ATTR_ID;
import static com.dianping.cat.home.service.client.Constants.ENTITY_METHOD;

import com.dianping.cat.home.service.client.BaseEntity;
import com.dianping.cat.home.service.client.IVisitor;

public class Method extends BaseEntity<Method> {
   private String m_id;

   private String m_service;

   private long m_totalCount;

   private long m_failureCount;

   private double m_failurePercent;

   private double m_sum;

   private double m_avg;

   private double m_qps;

   private double m_timeout;

   public Method() {
   }

   public Method(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitMethod(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Method) {
         Method _o = (Method) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
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

   public double getQps() {
      return m_qps;
   }

   public String getService() {
      return m_service;
   }

   public double getSum() {
      return m_sum;
   }

   public double getTimeout() {
      return m_timeout;
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

   public Method incFailureCount() {
      m_failureCount++;
      return this;
   }

   public Method incFailureCount(long failureCount) {
      m_failureCount += failureCount;
      return this;
   }

   public Method incSum() {
      m_sum++;
      return this;
   }

   public Method incSum(double sum) {
      m_sum += sum;
      return this;
   }

   public Method incTotalCount() {
      m_totalCount++;
      return this;
   }

   public Method incTotalCount(long totalCount) {
      m_totalCount += totalCount;
      return this;
   }

   @Override
   public void mergeAttributes(Method other) {
      assertAttributeEquals(other, ENTITY_METHOD, ATTR_ID, m_id, other.getId());

      if (other.getService() != null) {
         m_service = other.getService();
      }

      m_totalCount = other.getTotalCount();

      m_failureCount = other.getFailureCount();

      m_failurePercent = other.getFailurePercent();

      m_sum = other.getSum();

      m_avg = other.getAvg();

      m_qps = other.getQps();

      m_timeout = other.getTimeout();
   }

   public Method setAvg(double avg) {
      m_avg = avg;
      return this;
   }

   public Method setFailureCount(long failureCount) {
      m_failureCount = failureCount;
      return this;
   }

   public Method setFailurePercent(double failurePercent) {
      m_failurePercent = failurePercent;
      return this;
   }

   public Method setId(String id) {
      m_id = id;
      return this;
   }

   public Method setQps(double qps) {
      m_qps = qps;
      return this;
   }

   public Method setService(String service) {
      m_service = service;
      return this;
   }

   public Method setSum(double sum) {
      m_sum = sum;
      return this;
   }

   public Method setTimeout(double timeout) {
      m_timeout = timeout;
      return this;
   }

   public Method setTotalCount(long totalCount) {
      m_totalCount = totalCount;
      return this;
   }

}
