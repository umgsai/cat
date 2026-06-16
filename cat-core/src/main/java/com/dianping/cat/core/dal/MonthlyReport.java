package com.dianping.cat.core.dal;


public class MonthlyReport {
   private int m_id;

   private String m_name;

   private String m_ip;

   private String m_domain;

   private java.util.Date m_period;

   private int m_type;

   private java.util.Date m_creationDate;

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

   public int getId() {
      return m_id;
   }

   public String getIp() {
      return m_ip;
   }

   public int getKeyId() {
      return m_keyId;
   }

   public String getName() {
      return m_name;
   }

   public java.util.Date getPeriod() {
      return m_period;
   }

   public int getType() {
      return m_type;
   }

   public MonthlyReport setCreationDate(java.util.Date creationDate) {
      m_creationDate = creationDate;
      return this;
   }

   public MonthlyReport setDomain(String domain) {
      m_domain = domain;
      return this;
   }

   public MonthlyReport setId(int id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public MonthlyReport setIp(String ip) {
      m_ip = ip;
      return this;
   }

   public MonthlyReport setKeyId(int keyId) {
      m_keyId = keyId;
      return this;
   }

   public MonthlyReport setName(String name) {
      m_name = name;
      return this;
   }

   public MonthlyReport setPeriod(java.util.Date period) {
      m_period = period;
      return this;
   }

   public MonthlyReport setType(int type) {
      m_type = type;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("MonthlyReport[");
      sb.append("creation-date: ").append(m_creationDate);
      sb.append(", domain: ").append(m_domain);
      sb.append(", id: ").append(m_id);
      sb.append(", ip: ").append(m_ip);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", name: ").append(m_name);
      sb.append(", period: ").append(m_period);
      sb.append(", type: ").append(m_type);
      sb.append("]");
      return sb.toString();
   }

}
