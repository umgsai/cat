package com.dianping.cat.home.graph.entity;

import static com.dianping.cat.home.graph.Constants.ATTR_ID;
import static com.dianping.cat.home.graph.Constants.ENTITY_ITEM;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.graph.BaseEntity;
import com.dianping.cat.home.graph.IVisitor;

public class Item extends BaseEntity<Item> {
   private String m_id;

   private String m_view;

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

   public String getId() {
      return m_id;
   }

   public Map<String, Segment> getSegments() {
      return m_segments;
   }

   public String getView() {
      return m_view;
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

      if (other.getView() != null) {
         m_view = other.getView();
      }
   }

   public Segment removeSegment(String id) {
      return m_segments.remove(id);
   }

   public Item setId(String id) {
      m_id = id;
      return this;
   }

   public Item setView(String view) {
      m_view = view;
      return this;
   }

}
