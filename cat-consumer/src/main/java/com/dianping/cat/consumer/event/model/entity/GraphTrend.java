package com.dianping.cat.consumer.event.model.entity;

import static com.dianping.cat.consumer.event.model.Constants.ATTR_DURATION;
import static com.dianping.cat.consumer.event.model.Constants.ENTITY_GRAPH_TREND;

import com.dianping.cat.consumer.event.model.BaseEntity;
import com.dianping.cat.consumer.event.model.IVisitor;

public class GraphTrend extends BaseEntity<GraphTrend> {
   private int m_duration;

   private String m_count = "";

   private String m_fails = "";

   public GraphTrend() {
   }

   public GraphTrend(int duration) {
      m_duration = duration;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitGraphTrend(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof GraphTrend) {
         GraphTrend _o = (GraphTrend) obj;

         if (getDuration() != _o.getDuration()) {
            return false;
         }

         return true;
      }

      return false;
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

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + m_duration;

      return hash;
   }

   @Override
   public void mergeAttributes(GraphTrend other) {
      assertAttributeEquals(other, ENTITY_GRAPH_TREND, ATTR_DURATION, m_duration, other.getDuration());

      m_count = other.getCount();

      m_fails = other.getFails();
   }

   public GraphTrend setCount(String count) {
      m_count = count;
      return this;
   }

   public GraphTrend setDuration(int duration) {
      m_duration = duration;
      return this;
   }

   public GraphTrend setFails(String fails) {
      m_fails = fails;
      return this;
   }

}
