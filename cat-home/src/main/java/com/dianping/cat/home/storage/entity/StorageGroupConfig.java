package com.dianping.cat.home.storage.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.storage.BaseEntity;
import com.dianping.cat.home.storage.IVisitor;

public class StorageGroupConfig extends BaseEntity<StorageGroupConfig> {
   private Map<String, StorageGroup> m_storageGroups = new LinkedHashMap<String, StorageGroup>();

   public StorageGroupConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitStorageGroupConfig(this);
   }

   public StorageGroupConfig addStorageGroup(StorageGroup storageGroup) {
      m_storageGroups.put(storageGroup.getId(), storageGroup);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof StorageGroupConfig) {
         StorageGroupConfig _o = (StorageGroupConfig) obj;

         if (!equals(getStorageGroups(), _o.getStorageGroups())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public StorageGroup findStorageGroup(String id) {
      return m_storageGroups.get(id);
   }

   public Map<String, StorageGroup> getStorageGroups() {
      return m_storageGroups;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_storageGroups == null ? 0 : m_storageGroups.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(StorageGroupConfig other) {
   }

   public StorageGroup removeStorageGroup(String id) {
      return m_storageGroups.remove(id);
   }

}
