package com.dianping.cat.home.server.entity;

import static com.dianping.cat.home.server.Constants.ATTR_ID;
import static com.dianping.cat.home.server.Constants.ENTITY_ITEM;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.server.BaseEntity;
import com.dianping.cat.home.server.IVisitor;

public class Item extends BaseEntity<Item> {
   private String m_id;

   private Map<String, Segment> m_segments = new LinkedHashMap<String, Segment>();

   public Item() {
   }

   public Item(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitItem(this);
   }

   public Item addSegment(Segment segment) {
      m_segments.put(segment.getId(), segment);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Item) {
         Item _o = (Item) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public Segment findSegment(String id) {
      return m_segments.get(id);
   }

   public Segment findOrCreateSegment(String id) {
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

   public Map<String, Segment> getSegments() {
      return m_segments;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(Item other) {
      assertAttributeEquals(other, ENTITY_ITEM, ATTR_ID, m_id, other.getId());

   }

   public Segment removeSegment(String id) {
      return m_segments.remove(id);
   }

   public Item setId(String id) {
      m_id = id;
      return this;
   }

}
