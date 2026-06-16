package com.dianping.cat.configuration.business.entity;

import static com.dianping.cat.configuration.business.Constants.ATTR_ID;
import static com.dianping.cat.configuration.business.Constants.ENTITY_CUSTOM_CONFIG;

import com.dianping.cat.configuration.business.BaseEntity;
import com.dianping.cat.configuration.business.IVisitor;

public class CustomConfig extends BaseEntity<CustomConfig> {
   private String m_id;

   private double m_viewOrder;

   private String m_title;

   private boolean m_alarm = false;

   private boolean m_privilege = false;

   private String m_pattern;

   public CustomConfig() {
   }

   public CustomConfig(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitCustomConfig(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof CustomConfig) {
         CustomConfig _o = (CustomConfig) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public boolean getAlarm() {
      return m_alarm;
   }

   public String getId() {
      return m_id;
   }

   public String getPattern() {
      return m_pattern;
   }

   public boolean getPrivilege() {
      return m_privilege;
   }

   public String getTitle() {
      return m_title;
   }

   public double getViewOrder() {
      return m_viewOrder;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   public boolean isAlarm() {
      return m_alarm;
   }

   public boolean isPrivilege() {
      return m_privilege;
   }

   @Override
   public void mergeAttributes(CustomConfig other) {
      assertAttributeEquals(other, ENTITY_CUSTOM_CONFIG, ATTR_ID, m_id, other.getId());

      m_viewOrder = other.getViewOrder();

      if (other.getTitle() != null) {
         m_title = other.getTitle();
      }

      m_alarm = other.getAlarm();

      m_privilege = other.getPrivilege();
   }

   public CustomConfig setAlarm(boolean alarm) {
      m_alarm = alarm;
      return this;
   }

   public CustomConfig setId(String id) {
      m_id = id;
      return this;
   }

   public CustomConfig setPattern(String pattern) {
      m_pattern = pattern;
      return this;
   }

   public CustomConfig setPrivilege(boolean privilege) {
      m_privilege = privilege;
      return this;
   }

   public CustomConfig setTitle(String title) {
      m_title = title;
      return this;
   }

   public CustomConfig setViewOrder(double viewOrder) {
      m_viewOrder = viewOrder;
      return this;
   }

}
