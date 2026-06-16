package com.dianping.cat.core.dal;


public class Hostinfo {
   private int m_id;

   private String m_ip;

   private String m_domain;

   private String m_hostname;

   private java.util.Date m_creationDate;

   private java.util.Date m_lastModifiedDate;

   private int m_keyId;
   public void afterLoad() {
      m_keyId = m_id;
   }

   public java.util.Date getCreationDate() {
      return m_creationDate;
   }

   public String getDomain() {
      return m_domain;
   }

   public String getHostname() {
      return m_hostname;
   }

   public int getId() {
      return m_id;
   }

   public String getIp() {
      return m_ip;
   }

   public int getKeyId() {
      return m_keyId;
   }

   public java.util.Date getLastModifiedDate() {
      return m_lastModifiedDate;
   }

   public Hostinfo setCreationDate(java.util.Date creationDate) {
      m_creationDate = creationDate;
      return this;
   }

   public Hostinfo setDomain(String domain) {
      m_domain = domain;
      return this;
   }

   public Hostinfo setHostname(String hostname) {
      m_hostname = hostname;
      return this;
   }

   public Hostinfo setId(int id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public Hostinfo setIp(String ip) {
      m_ip = ip;
      return this;
   }

   public Hostinfo setKeyId(int keyId) {
      m_keyId = keyId;
      return this;
   }

   public Hostinfo setLastModifiedDate(java.util.Date lastModifiedDate) {
      m_lastModifiedDate = lastModifiedDate;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("Hostinfo[");
      sb.append("creation-date: ").append(m_creationDate);
      sb.append(", domain: ").append(m_domain);
      sb.append(", hostname: ").append(m_hostname);
      sb.append(", id: ").append(m_id);
      sb.append(", ip: ").append(m_ip);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", last-modified-date: ").append(m_lastModifiedDate);
      sb.append("]");
      return sb.toString();
   }

}
