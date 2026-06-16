package com.dianping.cat.home.server.entity;

import static com.dianping.cat.home.server.Constants.ATTR_ID;
import static com.dianping.cat.home.server.Constants.ENTITY_GROUP;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.server.BaseEntity;
import com.dianping.cat.home.server.IVisitor;

public class Group extends BaseEntity<Group> {
   private String m_id;

   private Map<String, Item> m_items = new LinkedHashMap<String, Item>();

   public Group() {
   }

   public Group(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitGroup(this);
   }

   public Group addItem(Item item) {
      m_items.put(item.getId(), item);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Group) {
         Group _o = (Group) obj;

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

   public Item findOrCreateItem(String id) {
      Item item = m_items.get(id);

      if (item == null) {
         synchronized (m_items) {
            item = m_items.get(id);

            if (item == null) {
               item = new Item(id);
               m_items.put(id, item);
            }
         }
      }

      return item;
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
   public void mergeAttributes(Group other) {
      assertAttributeEquals(other, ENTITY_GROUP, ATTR_ID, m_id, other.getId());

   }

   public Item removeItem(String id) {
      return m_items.remove(id);
   }

   public Group setId(String id) {
      m_id = id;
      return this;
   }

}
