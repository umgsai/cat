package com.dianping.cat.configuration.business.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.configuration.business.BaseEntity;
import com.dianping.cat.configuration.business.IVisitor;

public class BusinessReportConfig extends BaseEntity<BusinessReportConfig> {
   private String m_id;

   private Map<String, BusinessItemConfig> m_businessItemConfigs = new LinkedHashMap<String, BusinessItemConfig>();

   private Map<String, CustomConfig> m_customConfigs = new LinkedHashMap<String, CustomConfig>();

   public BusinessReportConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitBusinessReportConfig(this);
   }

   public BusinessReportConfig addBusinessItemConfig(BusinessItemConfig businessItemConfig) {
      m_businessItemConfigs.put(businessItemConfig.getId(), businessItemConfig);
      return this;
   }

   public BusinessReportConfig addCustomConfig(CustomConfig customConfig) {
      m_customConfigs.put(customConfig.getId(), customConfig);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof BusinessReportConfig) {
         BusinessReportConfig _o = (BusinessReportConfig) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         if (!equals(getBusinessItemConfigs(), _o.getBusinessItemConfigs())) {
            return false;
         }

         if (!equals(getCustomConfigs(), _o.getCustomConfigs())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public BusinessItemConfig findBusinessItemConfig(String id) {
      return m_businessItemConfigs.get(id);
   }

   public CustomConfig findCustomConfig(String id) {
      return m_customConfigs.get(id);
   }

   public Map<String, BusinessItemConfig> getBusinessItemConfigs() {
      return m_businessItemConfigs;
   }

   public Map<String, CustomConfig> getCustomConfigs() {
      return m_customConfigs;
   }

   public String getId() {
      return m_id;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
      hash = hash * 31 + (m_businessItemConfigs == null ? 0 : m_businessItemConfigs.hashCode());
      hash = hash * 31 + (m_customConfigs == null ? 0 : m_customConfigs.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(BusinessReportConfig other) {
      if (other.getId() != null) {
         m_id = other.getId();
      }
   }

   public BusinessItemConfig removeBusinessItemConfig(String id) {
      return m_businessItemConfigs.remove(id);
   }

   public CustomConfig removeCustomConfig(String id) {
      return m_customConfigs.remove(id);
   }

   public BusinessReportConfig setId(String id) {
      m_id = id;
      return this;
   }

}
