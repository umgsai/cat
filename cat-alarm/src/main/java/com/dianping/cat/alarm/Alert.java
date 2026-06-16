package com.dianping.cat.alarm;


public class Alert {
   private int m_id;

   private String m_domain;

   private java.util.Date m_alertTime;

   private String m_category;

   private String m_type;

   private String m_content;

   private String m_metric;

   private java.util.Date m_creationDate;

   private int m_keyId;

   private java.util.Date m_startTime;

   private java.util.Date m_endTime;

   private String[] m_categories;
   public void afterLoad() {
      m_keyId = m_id;
   }

   public java.util.Date getAlertTime() {
      return m_alertTime;
   }

   public String[] getCategories() {
      return m_categories;
   }

   public String getCategory() {
      return m_category;
   }

   public String getContent() {
      return m_content;
   }

   public java.util.Date getCreationDate() {
      return m_creationDate;
   }

   public String getDomain() {
      return m_domain;
   }

   public java.util.Date getEndTime() {
      return m_endTime;
   }

   public int getId() {
      return m_id;
   }

   public int getKeyId() {
      return m_keyId;
   }

   public String getMetric() {
      return m_metric;
   }

   public java.util.Date getStartTime() {
      return m_startTime;
   }

   public String getType() {
      return m_type;
   }

   public Alert setAlertTime(java.util.Date alertTime) {
      m_alertTime = alertTime;
      return this;
   }

   public Alert setCategories(String[] categories) {
      m_categories = categories;
      return this;
   }

   public Alert setCategory(String category) {
      m_category = category;
      return this;
   }

   public Alert setContent(String content) {
      m_content = content;
      return this;
   }

   public Alert setCreationDate(java.util.Date creationDate) {
      m_creationDate = creationDate;
      return this;
   }

   public Alert setDomain(String domain) {
      m_domain = domain;
      return this;
   }

   public Alert setEndTime(java.util.Date endTime) {
      m_endTime = endTime;
      return this;
   }

   public Alert setId(int id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public Alert setKeyId(int keyId) {
      m_keyId = keyId;
      return this;
   }

   public Alert setMetric(String metric) {
      m_metric = metric;
      return this;
   }

   public Alert setStartTime(java.util.Date startTime) {
      m_startTime = startTime;
      return this;
   }

   public Alert setType(String type) {
      m_type = type;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("Alert[");
      sb.append("alert-time: ").append(m_alertTime);
      sb.append(", categories: ").append(m_categories == null ? null : java.util.Arrays.asList(m_categories));
      sb.append(", category: ").append(m_category);
      sb.append(", content: ").append(m_content);
      sb.append(", creation-date: ").append(m_creationDate);
      sb.append(", domain: ").append(m_domain);
      sb.append(", end-time: ").append(m_endTime);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", metric: ").append(m_metric);
      sb.append(", start-time: ").append(m_startTime);
      sb.append(", type: ").append(m_type);
      sb.append("]");
      return sb.toString();
   }

}
