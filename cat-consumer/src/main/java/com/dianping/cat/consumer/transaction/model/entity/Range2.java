package com.dianping.cat.consumer.transaction.model.entity;

import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_VALUE;
import static com.dianping.cat.consumer.transaction.model.Constants.ENTITY_RANGE2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.cat.consumer.transaction.model.BaseEntity;
import com.dianping.cat.consumer.transaction.model.IVisitor;

public class Range2 extends BaseEntity<Range2> {
   private int m_value;

   private int m_count;

   private double m_sum;

   private double m_avg;

   private int m_fails;

   private double m_min = 86400000d;

   private double m_max = -1d;

   private double m_line95Value;

   private double m_line99Value;

   private double m_line999Value;

   private double m_line90Value;

   private transient Map<Integer, AllDuration> m_allDurations = new ConcurrentHashMap<Integer, AllDuration>();

   private transient boolean m_clearDuration;

   private double m_line50Value;

   private double m_line9999Value;

   public Range2() {
   }

   public Range2(int value) {
      m_value = value;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitRange2(this);
   }

   public Range2 addAllDuration(AllDuration allDuration) {
      m_allDurations.put(allDuration.getValue(), allDuration);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Range2) {
         Range2 _o = (Range2) obj;

         if (getValue() != _o.getValue()) {
            return false;
         }

         return true;
      }

      return false;
   }

   public AllDuration findAllDuration(int value) {
      return m_allDurations.get(value);
   }

   public AllDuration findOrCreateAllDuration(int value) {
      AllDuration allDuration = m_allDurations.get(value);

      if (allDuration == null) {
         synchronized (m_allDurations) {
            allDuration = m_allDurations.get(value);

            if (allDuration == null) {
               allDuration = new AllDuration(value);
               m_allDurations.put(value, allDuration);
            }
         }
      }

      return allDuration;
   }

   public Map<Integer, AllDuration> getAllDurations() {
      return m_allDurations;
   }

   public double getAvg() {
      return m_avg;
   }

   public boolean getClearDuration() {
      return m_clearDuration;
   }

   public int getCount() {
      return m_count;
   }

   public int getFails() {
      return m_fails;
   }

   public double getLine50Value() {
      return m_line50Value;
   }

   public double getLine90Value() {
      return m_line90Value;
   }

   public double getLine95Value() {
      return m_line95Value;
   }

   public double getLine9999Value() {
      return m_line9999Value;
   }

   public double getLine999Value() {
      return m_line999Value;
   }

   public double getLine99Value() {
      return m_line99Value;
   }

   public double getMax() {
      return m_max;
   }

   public double getMin() {
      return m_min;
   }

   public double getSum() {
      return m_sum;
   }

   public int getValue() {
      return m_value;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + m_value;

      return hash;
   }

   public Range2 incCount() {
      m_count++;
      return this;
   }

   public Range2 incCount(int count) {
      m_count += count;
      return this;
   }

   public Range2 incFails() {
      m_fails++;
      return this;
   }

   public Range2 incFails(int fails) {
      m_fails += fails;
      return this;
   }

   public boolean isClearDuration() {
      return m_clearDuration;
   }

   @Override
   public void mergeAttributes(Range2 other) {
      assertAttributeEquals(other, ENTITY_RANGE2, ATTR_VALUE, m_value, other.getValue());

      m_count = other.getCount();

      m_sum = other.getSum();

      m_avg = other.getAvg();

      m_fails = other.getFails();

      m_min = other.getMin();

      m_max = other.getMax();

      m_line95Value = other.getLine95Value();

      m_line99Value = other.getLine99Value();

      m_line999Value = other.getLine999Value();

      m_line90Value = other.getLine90Value();

      m_clearDuration = other.getClearDuration();

      m_line50Value = other.getLine50Value();

      m_line9999Value = other.getLine9999Value();
   }

   public AllDuration removeAllDuration(int value) {
      return m_allDurations.remove(value);
   }

   public Range2 setAvg(double avg) {
      m_avg = avg;
      return this;
   }

   public Range2 setClearDuration(boolean clearDuration) {
      m_clearDuration = clearDuration;
      return this;
   }

   public Range2 setCount(int count) {
      m_count = count;
      return this;
   }

   public Range2 setFails(int fails) {
      m_fails = fails;
      return this;
   }

   public Range2 setLine50Value(double line50Value) {
      m_line50Value = line50Value;
      return this;
   }

   public Range2 setLine90Value(double line90Value) {
      m_line90Value = line90Value;
      return this;
   }

   public Range2 setLine95Value(double line95Value) {
      m_line95Value = line95Value;
      return this;
   }

   public Range2 setLine9999Value(double line9999Value) {
      m_line9999Value = line9999Value;
      return this;
   }

   public Range2 setLine999Value(double line999Value) {
      m_line999Value = line999Value;
      return this;
   }

   public Range2 setLine99Value(double line99Value) {
      m_line99Value = line99Value;
      return this;
   }

   public Range2 setMax(double max) {
      m_max = max;
      return this;
   }

   public Range2 setMin(double min) {
      m_min = min;
      return this;
   }

   public Range2 setSum(double sum) {
      m_sum = sum;
      return this;
   }

   public Range2 setValue(int value) {
      m_value = value;
      return this;
   }

}
