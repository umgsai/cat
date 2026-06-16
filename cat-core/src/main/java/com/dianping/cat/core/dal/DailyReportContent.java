package com.dianping.cat.core.dal;


public class DailyReportContent {
   private int m_reportId;

   private byte[] m_content;

   private java.util.Date m_creationDate;

   private int m_keyReportId;

   private double m_contentLength;

   private int m_startId;

   private int m_endId;

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

   public int getEndId() {
      return m_endId;
   }

   public int getKeyReportId() {
      return m_keyReportId;
   }

   public int getReportId() {
      return m_reportId;
   }

   public int getStartId() {
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

   public DailyReportContent setEndId(int endId) {
      m_endId = endId;
      return this;
   }

   public DailyReportContent setKeyReportId(int keyReportId) {
      m_keyReportId = keyReportId;
      return this;
   }

   public DailyReportContent setReportId(int reportId) {
      m_reportId = reportId;
      m_keyReportId = reportId;
      return this;
   }

   public DailyReportContent setStartId(int startId) {
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
