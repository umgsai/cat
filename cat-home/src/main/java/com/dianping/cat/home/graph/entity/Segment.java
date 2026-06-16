package com.dianping.cat.home.graph.entity;

import static com.dianping.cat.home.graph.Constants.ATTR_ID;
import static com.dianping.cat.home.graph.Constants.ENTITY_SEGMENT;

import com.dianping.cat.home.graph.BaseEntity;
import com.dianping.cat.home.graph.IVisitor;

public class Segment extends BaseEntity<Segment> {
   private String m_id;

   private String m_category;

   private String m_measure;

   private String m_endPoint;

   private String m_tags;

   private String m_type;

   public Segment() {
   }

   public Segment(String id) {
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

   public String getCategory() {
      return m_category;
   }

   public String getEndPoint() {
      return m_endPoint;
   }

   public String getId() {
      return m_id;
   }

   public String getMeasure() {
      return m_measure;
   }

   public String getTags() {
      return m_tags;
   }

   public String getType() {
      return m_type;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(Segment other) {
      assertAttributeEquals(other, ENTITY_SEGMENT, ATTR_ID, m_id, other.getId());

      if (other.getCategory() != null) {
         m_category = other.getCategory();
      }

      if (other.getMeasure() != null) {
         m_measure = other.getMeasure();
      }

      if (other.getEndPoint() != null) {
         m_endPoint = other.getEndPoint();
      }

      if (other.getTags() != null) {
         m_tags = other.getTags();
      }

      if (other.getType() != null) {
         m_type = other.getType();
      }
   }

   public Segment setCategory(String category) {
      m_category = category;
      return this;
   }

   public Segment setEndPoint(String endPoint) {
      m_endPoint = endPoint;
      return this;
   }

   public Segment setId(String id) {
      m_id = id;
      return this;
   }

   public Segment setMeasure(String measure) {
      m_measure = measure;
      return this;
   }

   public Segment setTags(String tags) {
      m_tags = tags;
      return this;
   }

   public Segment setType(String type) {
      m_type = type;
      return this;
   }

}
