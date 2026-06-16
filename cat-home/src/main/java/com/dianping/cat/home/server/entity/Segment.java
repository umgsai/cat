package com.dianping.cat.home.server.entity;

import static com.dianping.cat.home.server.Constants.ATTR_ID;
import static com.dianping.cat.home.server.Constants.ENTITY_SEGMENT;

import com.dianping.cat.home.server.BaseEntity;
import com.dianping.cat.home.server.IVisitor;

public class Segment extends BaseEntity<Segment> {
   private String m_id;

   private String m_category;

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

   public String getId() {
      return m_id;
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

      if (other.getType() != null) {
         m_type = other.getType();
      }
   }

   public Segment setCategory(String category) {
      m_category = category;
      return this;
   }

   public Segment setId(String id) {
      m_id = id;
      return this;
   }

   public Segment setType(String type) {
      m_type = type;
      return this;
   }

}
