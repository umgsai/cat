package com.dianping.cat.home.dal.report;


public class TopologyGraph {
   private long m_id;

   private String m_ip;

   private java.util.Date m_period;

   private int m_type;

   private byte[] m_content;

   private java.util.Date m_createTime;

   private java.util.Date m_updateTime;

   private long m_keyId;
   public void afterLoad() {
      m_keyId = m_id;
   }

   public byte[] getContent() {
      return m_content;
   }

   public java.util.Date getCreationDate() {
      return m_createTime;
   }

   public java.util.Date getCreateTime() {
      return m_createTime;
   }

   public long getId() {
      return m_id;
   }

   public String getIp() {
      return m_ip;
   }

   public long getKeyId() {
      return m_keyId;
   }

   public java.util.Date getPeriod() {
      return m_period;
   }

   public int getType() {
      return m_type;
   }

   public java.util.Date getUpdateTime() {
      return m_updateTime;
   }

   public TopologyGraph setContent(byte[] content) {
      m_content = content;
      return this;
   }

   public TopologyGraph setCreationDate(java.util.Date creationDate) {
      m_createTime = creationDate;
      return this;
   }

   public TopologyGraph setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
      return this;
   }

   public TopologyGraph setId(long id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public TopologyGraph setIp(String ip) {
      m_ip = ip;
      return this;
   }

   public TopologyGraph setKeyId(long keyId) {
      m_keyId = keyId;
      return this;
   }

   public TopologyGraph setPeriod(java.util.Date period) {
      m_period = period;
      return this;
   }

   public TopologyGraph setType(int type) {
      m_type = type;
      return this;
   }

   public TopologyGraph setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("TopologyGraph[");
      sb.append("content: ").append(m_content == null ? null : java.util.Arrays.asList(m_content));
      sb.append(", create-time: ").append(m_createTime);
      sb.append(", id: ").append(m_id);
      sb.append(", ip: ").append(m_ip);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", period: ").append(m_period);
      sb.append(", type: ").append(m_type);
      sb.append(", update-time: ").append(m_updateTime);
      sb.append("]");
      return sb.toString();
   }

}
