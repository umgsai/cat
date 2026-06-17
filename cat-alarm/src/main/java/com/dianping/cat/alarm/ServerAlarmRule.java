package com.dianping.cat.alarm;


public class ServerAlarmRule {
   private long m_id;

   private String m_category;

   private String m_endPoint;

   private String m_measurement;

   private String m_tags;

   private String m_content;

   private String m_type;

   private String m_creator;

   private java.util.Date m_createTime;

   private java.util.Date m_updateTime;

   private long m_keyId;
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
      return m_createTime;
   }

   public java.util.Date getCreateTime() {
      return m_createTime;
   }

   public String getCreator() {
      return m_creator;
   }

   public String getEndPoint() {
      return m_endPoint;
   }

   public long getId() {
      return m_id;
   }

   public long getKeyId() {
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
      return m_updateTime;
   }

   public java.util.Date getUpdateTime() {
      return m_updateTime;
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
      m_createTime = creationDate;
      return this;
   }

   public ServerAlarmRule setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
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

   public ServerAlarmRule setId(long id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public ServerAlarmRule setKeyId(int keyId) {
      m_keyId = keyId;
      return this;
   }

   public ServerAlarmRule setKeyId(long keyId) {
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
      m_updateTime = updatetime;
      return this;
   }

   public ServerAlarmRule setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("ServerAlarmRule[");
      sb.append("category: ").append(m_category);
      sb.append(", content: ").append(m_content);
      sb.append(", create-time: ").append(m_createTime);
      sb.append(", creator: ").append(m_creator);
      sb.append(", end-point: ").append(m_endPoint);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", measurement: ").append(m_measurement);
      sb.append(", tags: ").append(m_tags);
      sb.append(", type: ").append(m_type);
      sb.append(", update-time: ").append(m_updateTime);
      sb.append("]");
      return sb.toString();
   }

}
