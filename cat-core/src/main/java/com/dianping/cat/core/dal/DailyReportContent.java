package com.dianping.cat.core.dal;


public class DailyReportContent {
   private long m_reportId;

   private byte[] m_content;

   private java.util.Date m_creationDate;

   private long m_keyReportId;

   private double m_contentLength;

   private long m_startId;

   private long m_endId;

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

   public double getContentLength() {
      return m_contentLength;
   }

   public java.util.Date getCreationDate() {
      return m_creationDate;
   }

   public long getEndId() {
      return m_endId;
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

   public DailyReportContent setCapacity(double capacity) {
      m_capacity = capacity;
      return this;
   }

   public DailyReportContent setContent(byte[] content) {
      m_content = content;
      return this;
   }

   public DailyReportContent setContentLength(double contentLength) {
      m_contentLength = contentLength;
      return this;
   }

   public DailyReportContent setCreationDate(java.util.Date creationDate) {
      m_creationDate = creationDate;
      return this;
   }

   public DailyReportContent setEndId(long endId) {
      m_endId = endId;
      return this;
   }

   public DailyReportContent setKeyReportId(long keyReportId) {
      m_keyReportId = keyReportId;
      return this;
   }

   public DailyReportContent setReportId(long reportId) {
      m_reportId = reportId;
      m_keyReportId = reportId;
      return this;
   }

   public DailyReportContent setStartId(long startId) {
      m_startId = startId;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("DailyReportContent[");
      sb.append("capacity: ").append(m_capacity);
      sb.append(", content: ").append(m_content == null ? null : java.util.Arrays.asList(m_content));
      sb.append(", content-length: ").append(m_contentLength);
      sb.append(", creation-date: ").append(m_creationDate);
      sb.append(", end-id: ").append(m_endId);
      sb.append(", key-report-id: ").append(m_keyReportId);
      sb.append(", report-id: ").append(m_reportId);
      sb.append(", start-id: ").append(m_startId);
      sb.append("]");
      return sb.toString();
   }

}
