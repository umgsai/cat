package com.dianping.cat.core.dal;


public class Hostinfo {
   private long m_id;

   private String m_ip;

   private String m_domain;

   private String m_hostname;

   private java.util.Date m_createTime;

   private java.util.Date m_updateTime;

   private long m_keyId;
   public void afterLoad() {
      m_keyId = m_id;
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

   public String getHostname() {
      return m_hostname;
   }

   public long getId() {
      return m_id;
   }

   public String getIp() {
      return m_ip;
   }

   public long getKeyId() {
      return m_keyId;
   }

   public java.util.Date getLastModifiedDate() {
      return m_updateTime;
   }

   public java.util.Date getUpdateTime() {
      return m_updateTime;
   }

   public Hostinfo setCreationDate(java.util.Date creationDate) {
      m_createTime = creationDate;
      return this;
   }

   public Hostinfo setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
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

   public Hostinfo setId(long id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public Hostinfo setIp(String ip) {
      m_ip = ip;
      return this;
   }

   public Hostinfo setKeyId(long keyId) {
      m_keyId = keyId;
      return this;
   }

   public Hostinfo setLastModifiedDate(java.util.Date lastModifiedDate) {
      m_updateTime = lastModifiedDate;
      return this;
   }

   public Hostinfo setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("Hostinfo[");
      sb.append("create-time: ").append(m_createTime);
      sb.append(", domain: ").append(m_domain);
      sb.append(", hostname: ").append(m_hostname);
      sb.append(", id: ").append(m_id);
      sb.append(", ip: ").append(m_ip);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", update-time: ").append(m_updateTime);
      sb.append("]");
      return sb.toString();
   }

}
