package com.dianping.cat.home.dal.report;


public class MetricGraph {
   private long m_id;

   private long m_graphId;

   private String m_name;

   private String m_content;

   private java.util.Date m_createTime;

   private java.util.Date m_updateTime;

   private long m_keyId;

   private int m_number;
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

   public long getGraphId() {
      return m_graphId;
   }

   public long getId() {
      return m_id;
   }

   public long getKeyId() {
      return m_keyId;
   }

   public String getName() {
      return m_name;
   }

   public int getNumber() {
      return m_number;
   }

   public java.util.Date getUpdatetime() {
      return m_updateTime;
   }

   public java.util.Date getUpdateTime() {
      return m_updateTime;
   }

   public MetricGraph setContent(String content) {
      m_content = content;
      return this;
   }

   public MetricGraph setCreationDate(java.util.Date creationDate) {
      m_createTime = creationDate;
      return this;
   }

   public MetricGraph setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
      return this;
   }

   public MetricGraph setGraphId(long graphId) {
      m_graphId = graphId;
      return this;
   }

   public MetricGraph setId(int id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public MetricGraph setId(long id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public MetricGraph setKeyId(int keyId) {
      m_keyId = keyId;
      return this;
   }

   public MetricGraph setKeyId(long keyId) {
      m_keyId = keyId;
      return this;
   }

   public MetricGraph setName(String name) {
      m_name = name;
      return this;
   }

   public MetricGraph setNumber(int number) {
      m_number = number;
      return this;
   }

   public MetricGraph setUpdatetime(java.util.Date updatetime) {
      m_updateTime = updatetime;
      return this;
   }

   public MetricGraph setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("MetricGraph[");
      sb.append("content: ").append(m_content);
      sb.append(", create-time: ").append(m_createTime);
      sb.append(", graph-id: ").append(m_graphId);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", name: ").append(m_name);
      sb.append(", number: ").append(m_number);
      sb.append(", update-time: ").append(m_updateTime);
      sb.append("]");
      return sb.toString();
   }

}
