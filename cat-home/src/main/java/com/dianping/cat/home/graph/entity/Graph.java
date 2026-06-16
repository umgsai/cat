package com.dianping.cat.home.graph.entity;

import static com.dianping.cat.home.graph.Constants.ATTR_ID;
import static com.dianping.cat.home.graph.Constants.ENTITY_GRAPH;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.graph.BaseEntity;
import com.dianping.cat.home.graph.IVisitor;

public class Graph extends BaseEntity<Graph> {
   private String m_id;

   private Map<String, Item> m_items = new LinkedHashMap<String, Item>();

   public Graph() {
   }

   public Graph(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitGraph(this);
   }

   public Graph addItem(Item item) {
      m_items.put(item.getId(), item);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Graph) {
         Graph _o = (Graph) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public Item findItem(String id) {
      return m_items.get(id);
   }

   public String getId() {
      return m_id;
   }

   public Map<String, Item> getItems() {
      return m_items;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(Graph other) {
      assertAttributeEquals(other, ENTITY_GRAPH, ATTR_ID, m_id, other.getId());

   }

   public Item removeItem(String id) {
      return m_items.remove(id);
   }

   public Graph setId(String id) {
      m_id = id;
      return this;
   }

}
