package com.dianping.cat.core.dal;


public class WeeklyReportContent {
   private long m_reportId;

   private byte[] m_content;

   private java.util.Date m_createTime;

   private java.util.Date m_updateTime;

   private long m_keyReportId;

   private double m_contentLength;

   private double m_capacity;

   private long m_startId;
   public void afterLoad() {
      m_keyReportId = m_reportId;
   }

   public double getCapacity() {
      return m_capacity;
   }

   public byte[] getContent() {
      return m_content;
   }

   public double getContentLength() {
      return m_contentLength;
   }

   public java.util.Date getCreationDate() {
      return m_createTime;
   }

   public java.util.Date getCreateTime() {
      return m_createTime;
   }

   public long getKeyReportId() {
      return m_keyReportId;
   }

   public long getReportId() {
      return m_reportId;
   }

   public long getStartId() {
      return m_startId;
   }

   public java.util.Date getUpdateTime() {
      return m_updateTime;
   }

   public WeeklyReportContent setCapacity(double capacity) {
      m_capacity = capacity;
      return this;
   }

   public WeeklyReportContent setContent(byte[] content) {
      m_content = content;
      return this;
   }

   public WeeklyReportContent setContentLength(double contentLength) {
      m_contentLength = contentLength;
      return this;
   }

   public WeeklyReportContent setCreationDate(java.util.Date creationDate) {
      m_createTime = creationDate;
      return this;
   }

   public WeeklyReportContent setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
      return this;
   }

   public WeeklyReportContent setKeyReportId(long keyReportId) {
      m_keyReportId = keyReportId;
      return this;
   }

   public WeeklyReportContent setReportId(long reportId) {
      m_reportId = reportId;
      m_keyReportId = reportId;
      return this;
   }

   public WeeklyReportContent setStartId(long startId) {
      m_startId = startId;
      return this;
   }

   public WeeklyReportContent setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("WeeklyReportContent[");
      sb.append("capacity: ").append(m_capacity);
      sb.append(", content: ").append(m_content == null ? null : java.util.Arrays.asList(m_content));
      sb.append(", content-length: ").append(m_contentLength);
      sb.append(", create-time: ").append(m_createTime);
      sb.append(", key-report-id: ").append(m_keyReportId);
      sb.append(", report-id: ").append(m_reportId);
      sb.append(", start-id: ").append(m_startId);
      sb.append(", update-time: ").append(m_updateTime);
      sb.append("]");
      return sb.toString();
   }

}
