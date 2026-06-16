package com.dianping.cat.alarm;


public class UserDefineRule {
   private int m_id;

   private String m_content;

   private java.util.Date m_creationDate;

   private int m_keyId;

   private int m_maxId;
   public void afterLoad() {
      m_keyId = m_id;
   }

   public String getContent() {
      return m_content;
   }

   public java.util.Date getCreationDate() {
      return m_creationDate;
   }

   public int getId() {
      return m_id;
   }

   public int getKeyId() {
      return m_keyId;
   }

   public int getMaxId() {
      return m_maxId;
   }

   public UserDefineRule setContent(String content) {
      m_content = content;
      return this;
   }

   public UserDefineRule setCreationDate(java.util.Date creationDate) {
      m_creationDate = creationDate;
      return this;
   }

   public UserDefineRule setId(int id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public UserDefineRule setKeyId(int keyId) {
      m_keyId = keyId;
      return this;
   }

   public UserDefineRule setMaxId(int maxId) {
      m_maxId = maxId;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("UserDefineRule[");
      sb.append("content: ").append(m_content);
      sb.append(", creation-date: ").append(m_creationDate);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", max-id: ").append(m_maxId);
      sb.append("]");
      return sb.toString();
   }

}
