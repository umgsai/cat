package com.dianping.cat.consumer.transaction.model.entity;

import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_DURATION;
import static com.dianping.cat.consumer.transaction.model.Constants.ENTITY_GRAPH2;

import com.dianping.cat.consumer.transaction.model.BaseEntity;
import com.dianping.cat.consumer.transaction.model.IVisitor;

public class Graph2 extends BaseEntity<Graph2> {
   private int m_duration;

   private String m_sum = "";

   private String m_avg = "";

   private String m_count = "";

   private String m_fails = "";

   public Graph2() {
   }

   public Graph2(int duration) {
      m_duration = duration;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitGraph2(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Graph2) {
         Graph2 _o = (Graph2) obj;

         if (getDuration() != _o.getDuration()) {
            return false;
         }

         return true;
      }

      return false;
   }

   public String getAvg() {
      return m_avg;
   }

   public String getCount() {
      return m_count;
   }

   public int getDuration() {
      return m_duration;
   }

   public String getFails() {
      return m_fails;
   }

   public String getSum() {
      return m_sum;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + m_duration;

      return hash;
   }

   @Override
   public void mergeAttributes(Graph2 other) {
      assertAttributeEquals(other, ENTITY_GRAPH2, ATTR_DURATION, m_duration, other.getDuration());

      m_sum = other.getSum();

      m_avg = other.getAvg();

      m_count = other.getCount();

      m_fails = other.getFails();
   }

   public Graph2 setAvg(String avg) {
      m_avg = avg;
      return this;
   }

   public Graph2 setCount(String count) {
      m_count = count;
      return this;
   }

   public Graph2 setDuration(int duration) {
      m_duration = duration;
      return this;
   }

   public Graph2 setFails(String fails) {
      m_fails = fails;
      return this;
   }

   public Graph2 setSum(String sum) {
      m_sum = sum;
      return this;
   }

}
