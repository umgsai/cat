package com.dianping.cat.core.dal;


public class HourlyReportContent {
   private long m_reportId;

   private byte[] m_content;

   private java.util.Date m_period;

   private java.util.Date m_createTime;

   private java.util.Date m_updateTime;

   private long m_keyReportId;

   private long m_contentLength;

   private long m_startId;

   private double m_capacity;
   public void afterLoad() {
      m_keyReportId = m_reportId;
   }

   public double getCapacity() {
      return m_capacity;
   }

   public byte[] getContent() {
      return m_content;
   }

   public long getContentLength() {
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

   public java.util.Date getPeriod() {
      return m_period;
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

   public HourlyReportContent setCapacity(double capacity) {
      m_capacity = capacity;
      return this;
   }

   public HourlyReportContent setContent(byte[] content) {
      m_content = content;
      return this;
   }

   public HourlyReportContent setContentLength(long contentLength) {
      m_contentLength = contentLength;
      return this;
   }

   public HourlyReportContent setCreationDate(java.util.Date creationDate) {
      m_createTime = creationDate;
      return this;
   }

   public HourlyReportContent setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
      return this;
   }

   public HourlyReportContent setKeyReportId(long keyReportId) {
      m_keyReportId = keyReportId;
      return this;
   }

   public HourlyReportContent setPeriod(java.util.Date period) {
      m_period = period;
      return this;
   }

   public HourlyReportContent setReportId(long reportId) {
      m_reportId = reportId;
      m_keyReportId = reportId;
      return this;
   }

   public HourlyReportContent setStartId(long startId) {
      m_startId = startId;
      return this;
   }

   public HourlyReportContent setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("HourlyReportContent[");
      sb.append("capacity: ").append(m_capacity);
      sb.append(", content: ").append(m_content == null ? null : java.util.Arrays.asList(m_content));
      sb.append(", content-length: ").append(m_contentLength);
      sb.append(", create-time: ").append(m_createTime);
      sb.append(", key-report-id: ").append(m_keyReportId);
      sb.append(", period: ").append(m_period);
      sb.append(", report-id: ").append(m_reportId);
      sb.append(", start-id: ").append(m_startId);
      sb.append(", update-time: ").append(m_updateTime);
      sb.append("]");
      return sb.toString();
   }

}
