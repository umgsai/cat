package com.dianping.cat.consumer.event.model.entity;

import static com.dianping.cat.consumer.event.model.Constants.ATTR_IP;
import static com.dianping.cat.consumer.event.model.Constants.ENTITY_MACHINE;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.cat.consumer.event.model.BaseEntity;
import com.dianping.cat.consumer.event.model.IVisitor;

public class Machine extends BaseEntity<Machine> {
   private String m_ip;

   private Map<String, EventType> m_types = new ConcurrentHashMap<String, EventType>();

   public Machine() {
   }

   public Machine(String ip) {
      m_ip = ip;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitMachine(this);
   }

   public Machine addType(EventType type) {
      m_types.put(type.getId(), type);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Machine) {
         Machine _o = (Machine) obj;

         if (!equals(getIp(), _o.getIp())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public EventType findType(String id) {
      return m_types.get(id);
   }

   public EventType findOrCreateType(String id) {
      EventType type = m_types.get(id);

      if (type == null) {
         synchronized (m_types) {
            type = m_types.get(id);

            if (type == null) {
               type = new EventType(id);
               m_types.put(id, type);
            }
         }
      }

      return type;
   }

   public String getIp() {
      return m_ip;
   }

   public Map<String, EventType> getTypes() {
      return m_types;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_ip == null ? 0 : m_ip.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(Machine other) {
      assertAttributeEquals(other, ENTITY_MACHINE, ATTR_IP, m_ip, other.getIp());

   }

   public EventType removeType(String id) {
      return m_types.remove(id);
   }

   public Machine setIp(String ip) {
      m_ip = ip;
      return this;
   }

}
