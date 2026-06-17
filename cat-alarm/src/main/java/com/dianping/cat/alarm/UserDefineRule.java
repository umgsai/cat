package com.dianping.cat.alarm;


public class UserDefineRule {
   private long m_id;

   private String m_content;

   private java.util.Date m_createTime;

   private java.util.Date m_updateTime;

   private long m_keyId;

   private long m_maxId;
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

   public long getMaxId() {
      return m_maxId;
   }

   public java.util.Date getUpdateTime() {
      return m_updateTime;
   }

   public UserDefineRule setContent(String content) {
      m_content = content;
      return this;
   }

   public UserDefineRule setCreationDate(java.util.Date creationDate) {
      m_createTime = creationDate;
      return this;
   }

   public UserDefineRule setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
      return this;
   }

   public UserDefineRule setId(int id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public UserDefineRule setId(long id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public UserDefineRule setKeyId(int keyId) {
      m_keyId = keyId;
      return this;
   }

   public UserDefineRule setKeyId(long keyId) {
      m_keyId = keyId;
      return this;
   }

   public UserDefineRule setMaxId(int maxId) {
      m_maxId = maxId;
      return this;
   }

   public UserDefineRule setMaxId(long maxId) {
      m_maxId = maxId;
      return this;
   }

   public UserDefineRule setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("UserDefineRule[");
      sb.append("content: ").append(m_content);
      sb.append(", create-time: ").append(m_createTime);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", max-id: ").append(m_maxId);
      sb.append(", update-time: ").append(m_updateTime);
      sb.append("]");
      return sb.toString();
   }

}
