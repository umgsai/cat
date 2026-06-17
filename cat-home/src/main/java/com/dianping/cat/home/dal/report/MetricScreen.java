package com.dianping.cat.home.dal.report;


public class MetricScreen {
   private long m_id;

   private String m_name;

   private String m_graphName;

   private String m_view;

   private String m_endPoints;

   private String m_measurements;

   private String m_content;

   private java.util.Date m_createTime;

   private java.util.Date m_updateTime;

   private long m_keyId;
   public void afterLoad() {
      m_keyId = m_id;
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

   public String getEndPoints() {
      return m_endPoints;
   }

   public String getGraphName() {
      return m_graphName;
   }

   public long getId() {
      return m_id;
   }

   public long getKeyId() {
      return m_keyId;
   }

   public String getMeasurements() {
      return m_measurements;
   }

   public String getName() {
      return m_name;
   }

   public java.util.Date getUpdatetime() {
      return m_updateTime;
   }

   public java.util.Date getUpdateTime() {
      return m_updateTime;
   }

   public String getView() {
      return m_view;
   }

   public MetricScreen setContent(String content) {
      m_content = content;
      return this;
   }

   public MetricScreen setCreationDate(java.util.Date creationDate) {
      m_createTime = creationDate;
      return this;
   }

   public MetricScreen setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
      return this;
   }

   public MetricScreen setEndPoints(String endPoints) {
      m_endPoints = endPoints;
      return this;
   }

   public MetricScreen setGraphName(String graphName) {
      m_graphName = graphName;
      return this;
   }

   public MetricScreen setId(int id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public MetricScreen setId(long id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public MetricScreen setKeyId(int keyId) {
      m_keyId = keyId;
      return this;
   }

   public MetricScreen setKeyId(long keyId) {
      m_keyId = keyId;
      return this;
   }

   public MetricScreen setMeasurements(String measurements) {
      m_measurements = measurements;
      return this;
   }

   public MetricScreen setName(String name) {
      m_name = name;
      return this;
   }

   public MetricScreen setUpdatetime(java.util.Date updatetime) {
      m_updateTime = updatetime;
      return this;
   }

   public MetricScreen setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   public MetricScreen setView(String view) {
      m_view = view;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("MetricScreen[");
      sb.append("content: ").append(m_content);
      sb.append(", create-time: ").append(m_createTime);
      sb.append(", end-points: ").append(m_endPoints);
      sb.append(", graph-name: ").append(m_graphName);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", measurements: ").append(m_measurements);
      sb.append(", name: ").append(m_name);
      sb.append(", update-time: ").append(m_updateTime);
      sb.append(", view: ").append(m_view);
      sb.append("]");
      return sb.toString();
   }

}
