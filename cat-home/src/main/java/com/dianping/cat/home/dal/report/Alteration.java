package com.dianping.cat.home.dal.report;


public class Alteration {
   private long m_id;

   private String m_type;

   private String m_title;

   private String m_domain;

   private String m_hostname;

   private String m_ip;

   private java.util.Date m_changeTime;

   private String m_user;

   private String m_altGroup;

   private String m_content;

   private String m_url;

   private int m_status;

   private java.util.Date m_createTime;

   private java.util.Date m_updateTime;

   private long m_keyId;

   private java.util.Date m_startTime;

   private java.util.Date m_endTime;

   private String[] m_types;
   public void afterLoad() {
      m_keyId = m_id;
   }

   public String getAltGroup() {
      return m_altGroup;
   }

   public String getContent() {
      return m_content;
   }

   public java.util.Date getChangeTime() {
      return m_changeTime;
   }

   public java.util.Date getCreationDate() {
      return m_createTime;
   }

   public java.util.Date getCreateTime() {
      return m_createTime;
   }

   public java.util.Date getDate() {
      return m_changeTime;
   }

   public String getDomain() {
      return m_domain;
   }

   public java.util.Date getEndTime() {
      return m_endTime;
   }

   public String getHostname() {
      return m_hostname;
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

   public java.util.Date getStartTime() {
      return m_startTime;
   }

   public int getStatus() {
      return m_status;
   }

   public String getTitle() {
      return m_title;
   }

   public String getType() {
      return m_type;
   }

   public String[] getTypes() {
      return m_types;
   }

   public java.util.Date getUpdateTime() {
      return m_updateTime;
   }

   public String getUrl() {
      return m_url;
   }

   public String getUser() {
      return m_user;
   }

   public Alteration setAltGroup(String altGroup) {
      m_altGroup = altGroup;
      return this;
   }

   public Alteration setContent(String content) {
      m_content = content;
      return this;
   }

   public Alteration setChangeTime(java.util.Date changeTime) {
      m_changeTime = changeTime;
      return this;
   }

   public Alteration setCreationDate(java.util.Date creationDate) {
      m_createTime = creationDate;
      return this;
   }

   public Alteration setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
      return this;
   }

   public Alteration setDate(java.util.Date date) {
      m_changeTime = date;
      return this;
   }

   public Alteration setDomain(String domain) {
      m_domain = domain;
      return this;
   }

   public Alteration setEndTime(java.util.Date endTime) {
      m_endTime = endTime;
      return this;
   }

   public Alteration setHostname(String hostname) {
      m_hostname = hostname;
      return this;
   }

   public Alteration setId(long id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public Alteration setIp(String ip) {
      m_ip = ip;
      return this;
   }

   public Alteration setKeyId(long keyId) {
      m_keyId = keyId;
      return this;
   }

   public Alteration setStartTime(java.util.Date startTime) {
      m_startTime = startTime;
      return this;
   }

   public Alteration setStatus(int status) {
      m_status = status;
      return this;
   }

   public Alteration setTitle(String title) {
      m_title = title;
      return this;
   }

   public Alteration setType(String type) {
      m_type = type;
      return this;
   }

   public Alteration setTypes(String[] types) {
      m_types = types;
      return this;
   }

   public Alteration setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   public Alteration setUrl(String url) {
      m_url = url;
      return this;
   }

   public Alteration setUser(String user) {
      m_user = user;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("Alteration[");
      sb.append("alt-group: ").append(m_altGroup);
      sb.append(", change-time: ").append(m_changeTime);
      sb.append(", content: ").append(m_content);
      sb.append(", create-time: ").append(m_createTime);
      sb.append(", domain: ").append(m_domain);
      sb.append(", end-time: ").append(m_endTime);
      sb.append(", hostname: ").append(m_hostname);
      sb.append(", id: ").append(m_id);
      sb.append(", ip: ").append(m_ip);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", start-time: ").append(m_startTime);
      sb.append(", status: ").append(m_status);
      sb.append(", title: ").append(m_title);
      sb.append(", type: ").append(m_type);
      sb.append(", types: ").append(m_types == null ? null : java.util.Arrays.asList(m_types));
      sb.append(", update-time: ").append(m_updateTime);
      sb.append(", url: ").append(m_url);
      sb.append(", user: ").append(m_user);
      sb.append("]");
      return sb.toString();
   }

}
