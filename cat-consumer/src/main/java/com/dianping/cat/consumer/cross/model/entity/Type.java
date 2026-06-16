package com.dianping.cat.consumer.cross.model.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.consumer.cross.model.BaseEntity;
import com.dianping.cat.consumer.cross.model.IVisitor;

public class Type extends BaseEntity<Type> {
   private String m_id;

   private long m_totalCount;

   private int m_failCount;

   private double m_failPercent;

   private double m_avg;

   private double m_sum;

   private double m_tps;

   private Map<String, Name> m_names = new LinkedHashMap<String, Name>();

   public Type() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitType(this);
   }

   public Type addName(Name name) {
      m_names.put(name.getId(), name);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Type) {
         Type _o = (Type) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         if (getTotalCount() != _o.getTotalCount()) {
            return false;
         }

         if (getFailCount() != _o.getFailCount()) {
            return false;
         }

         if (getFailPercent() != _o.getFailPercent()) {
            return false;
         }

         if (getAvg() != _o.getAvg()) {
            return false;
         }

         if (getSum() != _o.getSum()) {
            return false;
         }

         if (getTps() != _o.getTps()) {
            return false;
         }

         if (!equals(getNames(), _o.getNames())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public Name findName(String id) {
      return m_names.get(id);
   }

   public Name findOrCreateName(String id) {
      Name name = m_names.get(id);

      if (name == null) {
         synchronized (m_names) {
            name = m_names.get(id);

            if (name == null) {
               name = new Name(id);
               m_names.put(id, name);
            }
         }
      }

      return name;
   }

   public double getAvg() {
      return m_avg;
   }

   public int getFailCount() {
      return m_failCount;
   }

   public double getFailPercent() {
      return m_failPercent;
   }

   public String getId() {
      return m_id;
   }

   public Map<String, Name> getNames() {
      return m_names;
   }

   public double getSum() {
      return m_sum;
   }

   public long getTotalCount() {
      return m_totalCount;
   }

   public double getTps() {
      return m_tps;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
      hash = hash * 31 + (int) (m_totalCount ^ (m_totalCount >>> 32));
      hash = hash * 31 + m_failCount;
      hash = hash * 31 + (int) (Double.doubleToLongBits(m_failPercent) ^ (Double.doubleToLongBits(m_failPercent) >>> 32));
      hash = hash * 31 + (int) (Double.doubleToLongBits(m_avg) ^ (Double.doubleToLongBits(m_avg) >>> 32));
      hash = hash * 31 + (int) (Double.doubleToLongBits(m_sum) ^ (Double.doubleToLongBits(m_sum) >>> 32));
      hash = hash * 31 + (int) (Double.doubleToLongBits(m_tps) ^ (Double.doubleToLongBits(m_tps) >>> 32));
      hash = hash * 31 + (m_names == null ? 0 : m_names.hashCode());

      return hash;
   }

   public Type incFailCount() {
      m_failCount++;
      return this;
   }

   public Type incFailCount(int failCount) {
      m_failCount += failCount;
      return this;
   }

   public Type incTotalCount() {
      m_totalCount++;
      return this;
   }

   public Type incTotalCount(long totalCount) {
      m_totalCount += totalCount;
      return this;
   }

   @Override
   public void mergeAttributes(Type other) {
      if (other.getId() != null) {
         m_id = other.getId();
      }

      m_totalCount = other.getTotalCount();

      m_failCount = other.getFailCount();

      m_failPercent = other.getFailPercent();

      m_avg = other.getAvg();

      m_sum = other.getSum();

      m_tps = other.getTps();
   }

   public Name removeName(String id) {
      return m_names.remove(id);
   }

   public Type setAvg(double avg) {
      m_avg = avg;
      return this;
   }

   public Type setFailCount(int failCount) {
      m_failCount = failCount;
      return this;
   }

   public Type setFailPercent(double failPercent) {
      m_failPercent = failPercent;
      return this;
   }

   public Type setId(String id) {
      m_id = id;
      return this;
   }

   public Type setSum(double sum) {
      m_sum = sum;
      return this;
   }

   public Type setTotalCount(long totalCount) {
      m_totalCount = totalCount;
      return this;
   }

   public Type setTps(double tps) {
      m_tps = tps;
      return this;
   }

}
