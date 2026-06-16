package com.dianping.cat.consumer.business.model.entity;

import static com.dianping.cat.consumer.business.model.Constants.ATTR_ID;
import static com.dianping.cat.consumer.business.model.Constants.ENTITY_SEGMENT;

import com.dianping.cat.consumer.business.model.BaseEntity;
import com.dianping.cat.consumer.business.model.IVisitor;

public class Segment extends BaseEntity<Segment> {
   private Integer m_id;

   private int m_count;

   private double m_sum;

   private double m_avg;

   public Segment() {
   }

   public Segment(Integer id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitSegment(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Segment) {
         Segment _o = (Segment) obj;

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

   public int getCount() {
      return m_count;
   }

   public Integer getId() {
      return m_id;
   }

   public double getSum() {
      return m_sum;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   public Segment incCount() {
      m_count++;
      return this;
   }

   public Segment incCount(int count) {
      m_count += count;
      return this;
   }

   public Segment incSum() {
      m_sum++;
      return this;
   }

   public Segment incSum(double sum) {
      m_sum += sum;
      return this;
   }

   @Override
   public void mergeAttributes(Segment other) {
      assertAttributeEquals(other, ENTITY_SEGMENT, ATTR_ID, m_id, other.getId());

      m_count = other.getCount();

      m_sum = other.getSum();

      m_avg = other.getAvg();
   }

   public Segment setAvg(double avg) {
      m_avg = avg;
      return this;
   }

   public Segment setCount(int count) {
      m_count = count;
      return this;
   }

   public Segment setId(Integer id) {
      m_id = id;
      return this;
   }

   public Segment setSum(double sum) {
      m_sum = sum;
      return this;
   }

}
