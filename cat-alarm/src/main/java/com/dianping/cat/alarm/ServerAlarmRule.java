package com.dianping.cat.alarm;


public class ServerAlarmRule {
   private int m_id;

   private String m_category;

   private String m_endPoint;

   private String m_measurement;

   private String m_tags;

   private String m_content;

   private String m_type;

   private String m_creator;

   private java.util.Date m_creationDate;

   private java.util.Date m_updatetime;

   private int m_keyId;
   public void afterLoad() {
      m_keyId = m_id;
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

   public String getCreator() {
      return m_creator;
   }

   public String getEndPoint() {
      return m_endPoint;
   }

   public int getId() {
      return m_id;
   }

   public int getKeyId() {
      return m_keyId;
   }

   public String getMeasurement() {
      return m_measurement;
   }

   public String getTags() {
      return m_tags;
   }

   public String getType() {
      return m_type;
   }

   public java.util.Date getUpdatetime() {
      return m_updatetime;
   }

   public ServerAlarmRule setCategory(String category) {
      m_category = category;
      return this;
   }

   public ServerAlarmRule setContent(String content) {
      m_content = content;
      return this;
   }

   public ServerAlarmRule setCreationDate(java.util.Date creationDate) {
      m_creationDate = creationDate;
      return this;
   }

   public ServerAlarmRule setCreator(String creator) {
      m_creator = creator;
      return this;
   }

   public ServerAlarmRule setEndPoint(String endPoint) {
      m_endPoint = endPoint;
      return this;
   }

   public ServerAlarmRule setId(int id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public ServerAlarmRule setKeyId(int keyId) {
      m_keyId = keyId;
      return this;
   }

   public ServerAlarmRule setMeasurement(String measurement) {
      m_measurement = measurement;
      return this;
   }

   public ServerAlarmRule setTags(String tags) {
      m_tags = tags;
      return this;
   }

   public ServerAlarmRule setType(String type) {
      m_type = type;
      return this;
   }

   public ServerAlarmRule setUpdatetime(java.util.Date updatetime) {
      m_updatetime = updatetime;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("ServerAlarmRule[");
      sb.append("category: ").append(m_category);
      sb.append(", content: ").append(m_content);
      sb.append(", creation-date: ").append(m_creationDate);
      sb.append(", creator: ").append(m_creator);
      sb.append(", end-point: ").append(m_endPoint);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", measurement: ").append(m_measurement);
      sb.append(", tags: ").append(m_tags);
      sb.append(", type: ").append(m_type);
      sb.append(", updatetime: ").append(m_updatetime);
      sb.append("]");
      return sb.toString();
   }

}
