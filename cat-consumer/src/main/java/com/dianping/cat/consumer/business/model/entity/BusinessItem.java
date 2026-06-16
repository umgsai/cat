package com.dianping.cat.consumer.business.model.entity;

import static com.dianping.cat.consumer.business.model.Constants.ATTR_ID;
import static com.dianping.cat.consumer.business.model.Constants.ENTITY_BUSINESS_ITEM;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.consumer.business.model.BaseEntity;
import com.dianping.cat.consumer.business.model.IVisitor;

public class BusinessItem extends BaseEntity<BusinessItem> {
   private String m_id;

   private String m_type;

   private Map<Integer, Segment> m_segments = new LinkedHashMap<Integer, Segment>();

   public BusinessItem() {
   }

   public BusinessItem(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitBusinessItem(this);
   }

   public BusinessItem addSegment(Segment segment) {
      m_segments.put(segment.getId(), segment);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof BusinessItem) {
         BusinessItem _o = (BusinessItem) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public Segment findSegment(Integer id) {
      return m_segments.get(id);
   }

   public Segment findOrCreateSegment(Integer id) {
      Segment segment = m_segments.get(id);

      if (segment == null) {
         synchronized (m_segments) {
            segment = m_segments.get(id);

            if (segment == null) {
               segment = new Segment(id);
               m_segments.put(id, segment);
            }
         }
      }

      return segment;
   }

   public String getId() {
      return m_id;
   }

   public Map<Integer, Segment> getSegments() {
      return m_segments;
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
   public void mergeAttributes(BusinessItem other) {
      assertAttributeEquals(other, ENTITY_BUSINESS_ITEM, ATTR_ID, m_id, other.getId());

      if (other.getType() != null) {
         m_type = other.getType();
      }
   }

   public Segment removeSegment(Integer id) {
      return m_segments.remove(id);
   }

   public BusinessItem setId(String id) {
      m_id = id;
      return this;
   }

   public BusinessItem setType(String type) {
      m_type = type;
      return this;
   }

}
