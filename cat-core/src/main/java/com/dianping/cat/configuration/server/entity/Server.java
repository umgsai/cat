package com.dianping.cat.configuration.server.entity;

import static com.dianping.cat.configuration.server.Constants.ATTR_ID;
import static com.dianping.cat.configuration.server.Constants.ENTITY_SERVER;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.configuration.server.BaseEntity;
import com.dianping.cat.configuration.server.IVisitor;

public class Server extends BaseEntity<Server> {
   private String m_id;

   private Map<String, Property> m_properties = new LinkedHashMap<String, Property>();

   private StorageConfig m_storage;

   private ConsumerConfig m_consumer;

   public Server() {
   }

   public Server(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitServer(this);
   }

   public Server addProperty(Property property) {
      m_properties.put(property.getName(), property);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Server) {
         Server _o = (Server) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public Property findProperty(String name) {
      return m_properties.get(name);
   }

   public ConsumerConfig getConsumer() {
      return m_consumer;
   }

   public String getId() {
      return m_id;
   }

   public Map<String, Property> getProperties() {
      return m_properties;
   }

   public StorageConfig getStorage() {
      return m_storage;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(Server other) {
      assertAttributeEquals(other, ENTITY_SERVER, ATTR_ID, m_id, other.getId());

   }

   public Property removeProperty(String name) {
      return m_properties.remove(name);
   }

   public Server setConsumer(ConsumerConfig consumer) {
      m_consumer = consumer;
      return this;
   }

   public Server setId(String id) {
      m_id = id;
      return this;
   }

   public Server setStorage(StorageConfig storage) {
      m_storage = storage;
      return this;
   }

}
