package com.dianping.cat.home.storage.alert.entity;

import static com.dianping.cat.home.storage.alert.Constants.ATTR_ID;
import static com.dianping.cat.home.storage.alert.Constants.ENTITY_STORAGE;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.storage.alert.BaseEntity;
import com.dianping.cat.home.storage.alert.IVisitor;

public class Storage extends BaseEntity<Storage> {
   private String m_id;

   private int m_level;

   private int m_count;

   private Map<String, Machine> m_machines = new LinkedHashMap<String, Machine>();

   public Storage() {
   }

   public Storage(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitStorage(this);
   }

   public Storage addMachine(Machine machine) {
      m_machines.put(machine.getId(), machine);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Storage) {
         Storage _o = (Storage) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public Machine findMachine(String id) {
      return m_machines.get(id);
   }

   public Machine findOrCreateMachine(String id) {
      Machine machine = m_machines.get(id);

      if (machine == null) {
         synchronized (m_machines) {
            machine = m_machines.get(id);

            if (machine == null) {
               machine = new Machine(id);
               m_machines.put(id, machine);
            }
         }
      }

      return machine;
   }

   public int getCount() {
      return m_count;
   }

   public String getId() {
      return m_id;
   }

   public int getLevel() {
      return m_level;
   }

   public Map<String, Machine> getMachines() {
      return m_machines;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   public Storage incCount() {
      m_count++;
      return this;
   }

   public Storage incCount(int count) {
      m_count += count;
      return this;
   }

   @Override
   public void mergeAttributes(Storage other) {
      assertAttributeEquals(other, ENTITY_STORAGE, ATTR_ID, m_id, other.getId());

      m_level = other.getLevel();

      m_count = other.getCount();
   }

   public Machine removeMachine(String id) {
      return m_machines.remove(id);
   }

   public Storage setCount(int count) {
      m_count = count;
      return this;
   }

   public Storage setId(String id) {
      m_id = id;
      return this;
   }

   public Storage setLevel(int level) {
      m_level = level;
      return this;
   }

}
