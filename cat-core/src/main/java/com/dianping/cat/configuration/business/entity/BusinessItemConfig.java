package com.dianping.cat.configuration.business.entity;

import static com.dianping.cat.configuration.business.Constants.ATTR_ID;
import static com.dianping.cat.configuration.business.Constants.ENTITY_BUSINESS_ITEM_CONFIG;

import com.dianping.cat.configuration.business.BaseEntity;
import com.dianping.cat.configuration.business.IVisitor;

public class BusinessItemConfig extends BaseEntity<BusinessItemConfig> {
   private String m_id;

   private double m_viewOrder;

   private String m_title;

   private boolean m_showCount;

   private boolean m_showAvg;

   private boolean m_showSum;

   private boolean m_alarm = false;

   private boolean m_privilege = false;

   public BusinessItemConfig() {
   }

   public BusinessItemConfig(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitBusinessItemConfig(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof BusinessItemConfig) {
         BusinessItemConfig _o = (BusinessItemConfig) obj;

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

   public boolean getPrivilege() {
      return m_privilege;
   }

   public boolean getShowAvg() {
      return m_showAvg;
   }

   public boolean getShowCount() {
      return m_showCount;
   }

   public boolean getShowSum() {
      return m_showSum;
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

   public boolean isShowAvg() {
      return m_showAvg;
   }

   public boolean isShowCount() {
      return m_showCount;
   }

   public boolean isShowSum() {
      return m_showSum;
   }

   @Override
   public void mergeAttributes(BusinessItemConfig other) {
      assertAttributeEquals(other, ENTITY_BUSINESS_ITEM_CONFIG, ATTR_ID, m_id, other.getId());

      m_viewOrder = other.getViewOrder();

      if (other.getTitle() != null) {
         m_title = other.getTitle();
      }

      m_showCount = other.getShowCount();

      m_showAvg = other.getShowAvg();

      m_showSum = other.getShowSum();

      m_alarm = other.getAlarm();

      m_privilege = other.getPrivilege();
   }

   public BusinessItemConfig setAlarm(boolean alarm) {
      m_alarm = alarm;
      return this;
   }

   public BusinessItemConfig setId(String id) {
      m_id = id;
      return this;
   }

   public BusinessItemConfig setPrivilege(boolean privilege) {
      m_privilege = privilege;
      return this;
   }

   public BusinessItemConfig setShowAvg(boolean showAvg) {
      m_showAvg = showAvg;
      return this;
   }

   public BusinessItemConfig setShowCount(boolean showCount) {
      m_showCount = showCount;
      return this;
   }

   public BusinessItemConfig setShowSum(boolean showSum) {
      m_showSum = showSum;
      return this;
   }

   public BusinessItemConfig setTitle(String title) {
      m_title = title;
      return this;
   }

   public BusinessItemConfig setViewOrder(double viewOrder) {
      m_viewOrder = viewOrder;
      return this;
   }

}
