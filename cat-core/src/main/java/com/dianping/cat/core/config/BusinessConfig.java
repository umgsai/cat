package com.dianping.cat.core.config;


public class BusinessConfig {
   private long m_id;

   private String m_name;

   private String m_domain;

   private String m_content;

   private java.util.Date m_createTime;

   private java.util.Date m_updateTime;

   private long m_keyId;
   public void afterLoad() {
      m_keyId = m_id;
   }

   public String getContent() {
      return m_content;
   }

   public String getDomain() {
      return m_domain;
   }

   public java.util.Date getCreateTime() {
      return m_createTime;
   }

   public long getId() {
      return m_id;
   }

   public long getKeyId() {
      return m_keyId;
   }

   public String getName() {
      return m_name;
   }

   public java.util.Date getUpdatetime() {
      return m_updateTime;
   }

   public java.util.Date getUpdateTime() {
      return m_updateTime;
   }

   public BusinessConfig setContent(String content) {
      m_content = content;
      return this;
   }

   public BusinessConfig setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
      return this;
   }

   public BusinessConfig setDomain(String domain) {
      m_domain = domain;
      return this;
   }

   public BusinessConfig setId(int id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public BusinessConfig setId(long id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public BusinessConfig setKeyId(int keyId) {
      m_keyId = keyId;
      return this;
   }

   public BusinessConfig setKeyId(long keyId) {
      m_keyId = keyId;
      return this;
   }

   public BusinessConfig setName(String name) {
      m_name = name;
      return this;
   }

   public BusinessConfig setUpdatetime(java.util.Date updatetime) {
      m_updateTime = updatetime;
      return this;
   }

   public BusinessConfig setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("BusinessConfig[");
      sb.append("content: ").append(m_content);
      sb.append(", create-time: ").append(m_createTime);
      sb.append(", domain: ").append(m_domain);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", name: ").append(m_name);
      sb.append(", update-time: ").append(m_updateTime);
      sb.append("]");
      return sb.toString();
   }

}
