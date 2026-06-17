package com.dianping.cat.home.dal.report;


public class Overload {
   private long m_id;

   private long m_reportId;

   private int m_reportType;

   private double m_reportSize;

   private java.util.Date m_period;

   private java.util.Date m_createTime;

   private java.util.Date m_updateTime;

   private long m_keyId;

   private java.util.Date m_startTime;

   private java.util.Date m_endTime;

   private int m_type;

   private long m_maxId;

   private long m_count;
   public void afterLoad() {
      m_keyId = m_id;
   }

   public long getCount() {
      return m_count;
   }

   public java.util.Date getCreateTime() {
      return m_createTime;
   }

   public java.util.Date getCreationDate() {
      return m_createTime;
   }

   public java.util.Date getEndTime() {
      return m_endTime;
   }

   public long getId() {
      return m_id;
   }

   public long getKeyId() {
      return m_keyId;
   }

   public long getMaxId() {
      return m_maxId;
   }

   public java.util.Date getPeriod() {
      return m_period;
   }

   public long getReportId() {
      return m_reportId;
   }

   public double getReportSize() {
      return m_reportSize;
   }

   public int getReportType() {
      return m_reportType;
   }

   public java.util.Date getStartTime() {
      return m_startTime;
   }

   public int getType() {
      return m_type;
   }

   public java.util.Date getUpdateTime() {
      return m_updateTime;
   }

   public Overload setCount(int count) {
      m_count = count;
      return this;
   }

   public Overload setCount(long count) {
      m_count = count;
      return this;
   }

   public Overload setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
      return this;
   }

   public Overload setCreationDate(java.util.Date creationDate) {
      m_createTime = creationDate;
      return this;
   }

   public Overload setEndTime(java.util.Date endTime) {
      m_endTime = endTime;
      return this;
   }

   public Overload setId(int id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public Overload setId(long id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public Overload setKeyId(int keyId) {
      m_keyId = keyId;
      return this;
   }

   public Overload setKeyId(long keyId) {
      m_keyId = keyId;
      return this;
   }

   public Overload setMaxId(int maxId) {
      m_maxId = maxId;
      return this;
   }

   public Overload setMaxId(long maxId) {
      m_maxId = maxId;
      return this;
   }

   public Overload setPeriod(java.util.Date period) {
      m_period = period;
      return this;
   }

   public Overload setReportId(int reportId) {
      m_reportId = reportId;
      return this;
   }

   public Overload setReportId(long reportId) {
      m_reportId = reportId;
      return this;
   }

   public Overload setReportSize(double reportSize) {
      m_reportSize = reportSize;
      return this;
   }

   public Overload setReportType(int reportType) {
      m_reportType = reportType;
      return this;
   }

   public Overload setStartTime(java.util.Date startTime) {
      m_startTime = startTime;
      return this;
   }

   public Overload setType(int type) {
      m_type = type;
      return this;
   }

   public Overload setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("Overload[");
      sb.append("count: ").append(m_count);
      sb.append(", create-time: ").append(m_createTime);
      sb.append(", end-time: ").append(m_endTime);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", max-id: ").append(m_maxId);
      sb.append(", period: ").append(m_period);
      sb.append(", report-id: ").append(m_reportId);
      sb.append(", report-size: ").append(m_reportSize);
      sb.append(", report-type: ").append(m_reportType);
      sb.append(", start-time: ").append(m_startTime);
      sb.append(", type: ").append(m_type);
      sb.append(", update-time: ").append(m_updateTime);
      sb.append("]");
      return sb.toString();
   }

}
