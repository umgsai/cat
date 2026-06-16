package com.dianping.cat.core.config;


public class Config {
   private int m_id;

   private String m_name;

   private String m_content;

   private java.util.Date m_creationDate;

   private java.util.Date m_modifyDate;

   private int m_keyId;
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

   public java.util.Date getModifyDate() {
      return m_modifyDate;
   }

   public String getName() {
      return m_name;
   }

   public Config setContent(String content) {
      m_content = content;
      return this;
   }

   public Config setCreationDate(java.util.Date creationDate) {
      m_creationDate = creationDate;
      return this;
   }

   public Config setId(int id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public Config setKeyId(int keyId) {
      m_keyId = keyId;
      return this;
   }

   public Config setModifyDate(java.util.Date modifyDate) {
      m_modifyDate = modifyDate;
      return this;
   }

   public Config setName(String name) {
      m_name = name;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("Config[");
      sb.append("content: ").append(m_content);
      sb.append(", creation-date: ").append(m_creationDate);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", modify-date: ").append(m_modifyDate);
      sb.append(", name: ").append(m_name);
      sb.append("]");
      return sb.toString();
   }

}
