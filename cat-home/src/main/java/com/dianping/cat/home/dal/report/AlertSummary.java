package com.dianping.cat.home.dal.report;


public class AlertSummary {
   private long m_id;

   private String m_domain;

   private java.util.Date m_alertTime;

   private String m_content;

   private java.util.Date m_createTime;

   private java.util.Date m_updateTime;

   private long m_keyId;
   public void afterLoad() {
      m_keyId = m_id;
   }

   public java.util.Date getAlertTime() {
      return m_alertTime;
   }

   public String getContent() {
      return m_content;
   }

   public java.util.Date getCreationDate() {
      return m_createTime;
   }

   public java.util.Date getCreateTime() {
      return m_createTime;
   }

   public String getDomain() {
      return m_domain;
   }

   public long getId() {
      return m_id;
   }

   public long getKeyId() {
      return m_keyId;
   }

   public java.util.Date getUpdateTime() {
      return m_updateTime;
   }

   public AlertSummary setAlertTime(java.util.Date alertTime) {
      m_alertTime = alertTime;
      return this;
   }

   public AlertSummary setContent(String content) {
      m_content = content;
      return this;
   }

   public AlertSummary setCreationDate(java.util.Date creationDate) {
      m_createTime = creationDate;
      return this;
   }

   public AlertSummary setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
      return this;
   }

   public AlertSummary setDomain(String domain) {
      m_domain = domain;
      return this;
   }

   public AlertSummary setId(long id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public AlertSummary setKeyId(long keyId) {
      m_keyId = keyId;
      return this;
   }

   public AlertSummary setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("AlertSummary[");
      sb.append("alert-time: ").append(m_alertTime);
      sb.append(", content: ").append(m_content);
      sb.append(", create-time: ").append(m_createTime);
      sb.append(", domain: ").append(m_domain);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", update-time: ").append(m_updateTime);
      sb.append("]");
      return sb.toString();
   }

}
