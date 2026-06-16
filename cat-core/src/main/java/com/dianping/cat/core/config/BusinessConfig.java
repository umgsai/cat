package com.dianping.cat.core.config;


public class BusinessConfig {
   private int m_id;

   private String m_name;

   private String m_domain;

   private String m_content;

   private java.util.Date m_updatetime;

   private int m_keyId;
   public void afterLoad() {
      m_keyId = m_id;
   }

   public String getContent() {
      return m_content;
   }

   public String getDomain() {
      return m_domain;
   }

   public int getId() {
      return m_id;
   }

   public int getKeyId() {
      return m_keyId;
   }

   public String getName() {
      return m_name;
   }

   public java.util.Date getUpdatetime() {
      return m_updatetime;
   }

   public BusinessConfig setContent(String content) {
      m_content = content;
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

   public BusinessConfig setKeyId(int keyId) {
      m_keyId = keyId;
      return this;
   }

   public BusinessConfig setName(String name) {
      m_name = name;
      return this;
   }

   public BusinessConfig setUpdatetime(java.util.Date updatetime) {
      m_updatetime = updatetime;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("BusinessConfig[");
      sb.append("content: ").append(m_content);
      sb.append(", domain: ").append(m_domain);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", name: ").append(m_name);
      sb.append(", updatetime: ").append(m_updatetime);
      sb.append("]");
      return sb.toString();
   }

}
