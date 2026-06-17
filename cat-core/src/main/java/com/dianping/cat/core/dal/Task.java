package com.dianping.cat.core.dal;


public class Task {
   private long m_id;

   private String m_producer;

   private String m_consumer;

   private int m_failureCount;

   private String m_reportName;

   private String m_reportDomain;

   private java.util.Date m_reportPeriod;

   private int m_status;

   private int m_taskType;

   private java.util.Date m_createTime;

   private java.util.Date m_startTime;

   private java.util.Date m_endTime;

   private long m_keyId;

   private java.util.Date m_updateTime;

   private int m_count;

   private int m_startLimit;

   private int m_endLimit;
   public void afterLoad() {
      m_keyId = m_id;
   }

   public String getConsumer() {
      return m_consumer;
   }

   public int getCount() {
      return m_count;
   }

   public java.util.Date getCreationDate() {
      return m_createTime;
   }

   public java.util.Date getCreateTime() {
      return m_createTime;
   }

   public java.util.Date getEndDate() {
      return m_endTime;
   }

   public int getEndLimit() {
      return m_endLimit;
   }

   public int getFailureCount() {
      return m_failureCount;
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

   public String getProducer() {
      return m_producer;
   }

   public String getReportDomain() {
      return m_reportDomain;
   }

   public String getReportName() {
      return m_reportName;
   }

   public java.util.Date getReportPeriod() {
      return m_reportPeriod;
   }

   public java.util.Date getStartDate() {
      return m_startTime;
   }

   public int getStartLimit() {
      return m_startLimit;
   }

   public int getStatus() {
      return m_status;
   }

   public int getTaskType() {
      return m_taskType;
   }

   public java.util.Date getStartTime() {
      return m_startTime;
   }

   public java.util.Date getUpdateTime() {
      return m_updateTime;
   }

   public Task setConsumer(String consumer) {
      m_consumer = consumer;
      return this;
   }

   public Task setCount(int count) {
      m_count = count;
      return this;
   }

   public Task setCreationDate(java.util.Date creationDate) {
      m_createTime = creationDate;
      return this;
   }

   public Task setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
      return this;
   }

   public Task setEndDate(java.util.Date endDate) {
      m_endTime = endDate;
      return this;
   }

   public Task setEndLimit(int endLimit) {
      m_endLimit = endLimit;
      return this;
   }

   public Task setFailureCount(int failureCount) {
      m_failureCount = failureCount;
      return this;
   }

   public Task setEndTime(java.util.Date endTime) {
      m_endTime = endTime;
      return this;
   }

   public Task setId(long id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public Task setKeyId(long keyId) {
      m_keyId = keyId;
      return this;
   }

   public Task setProducer(String producer) {
      m_producer = producer;
      return this;
   }

   public Task setReportDomain(String reportDomain) {
      m_reportDomain = reportDomain;
      return this;
   }

   public Task setReportName(String reportName) {
      m_reportName = reportName;
      return this;
   }

   public Task setReportPeriod(java.util.Date reportPeriod) {
      m_reportPeriod = reportPeriod;
      return this;
   }

   public Task setStartDate(java.util.Date startDate) {
      m_startTime = startDate;
      return this;
   }

   public Task setStartLimit(int startLimit) {
      m_startLimit = startLimit;
      return this;
   }

   public Task setStatus(int status) {
      m_status = status;
      return this;
   }

   public Task setTaskType(int taskType) {
      m_taskType = taskType;
      return this;
   }

   public Task setStartTime(java.util.Date startTime) {
      m_startTime = startTime;
      return this;
   }

   public Task setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("Task[");
      sb.append("consumer: ").append(m_consumer);
      sb.append(", count: ").append(m_count);
      sb.append(", create-time: ").append(m_createTime);
      sb.append(", end-time: ").append(m_endTime);
      sb.append(", end-limit: ").append(m_endLimit);
      sb.append(", failure-count: ").append(m_failureCount);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", producer: ").append(m_producer);
      sb.append(", report-domain: ").append(m_reportDomain);
      sb.append(", report-name: ").append(m_reportName);
      sb.append(", report-period: ").append(m_reportPeriod);
      sb.append(", start-time: ").append(m_startTime);
      sb.append(", start-limit: ").append(m_startLimit);
      sb.append(", status: ").append(m_status);
      sb.append(", task-type: ").append(m_taskType);
      sb.append(", update-time: ").append(m_updateTime);
      sb.append("]");
      return sb.toString();
   }

}
