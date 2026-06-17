package com.dianping.cat.core.config;


public class Config {
   private long m_id;

   private String m_name;

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

   public java.util.Date getCreationDate() {
      return m_createTime;
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

   public java.util.Date getModifyDate() {
      return m_updateTime;
   }

   public String getName() {
      return m_name;
   }

   public java.util.Date getUpdateTime() {
      return m_updateTime;
   }

   public Config setContent(String content) {
      m_content = content;
      return this;
   }

   public Config setCreationDate(java.util.Date creationDate) {
      m_createTime = creationDate;
      return this;
   }

   public Config setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
      return this;
   }

   public Config setId(long id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public Config setKeyId(long keyId) {
      m_keyId = keyId;
      return this;
   }

   public Config setModifyDate(java.util.Date modifyDate) {
      m_updateTime = modifyDate;
      return this;
   }

   public Config setName(String name) {
      m_name = name;
      return this;
   }

   public Config setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("Config[");
      sb.append("content: ").append(m_content);
      sb.append(", create-time: ").append(m_createTime);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", update-time: ").append(m_updateTime);
      sb.append(", name: ").append(m_name);
      sb.append("]");
      return sb.toString();
   }

}
