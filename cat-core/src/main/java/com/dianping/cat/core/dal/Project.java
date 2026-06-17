package com.dianping.cat.core.dal;


public class Project {
   private long m_id;

   private String m_domain;

   private String m_cmdbDomain;

   private int m_level;

   private String m_bu;

   private String m_cmdbProductline;

   private String m_owner;

   private String m_email;

   private String m_phone;

   private java.util.Date m_createTime;

   private java.util.Date m_updateTime;

   private long m_keyId;
   public void afterLoad() {
      m_keyId = m_id;
   }

   public String getBu() {
      return m_bu;
   }

   public String getCmdbDomain() {
      return m_cmdbDomain;
   }

   public String getCmdbProductline() {
      return m_cmdbProductline;
   }

   public java.util.Date getCreationDate() {
      return m_createTime;
   }

   public java.util.Date getCreateTime() {
      return m_createTime;
   }

   public String getDomain() {
      return m_domain;
   }

   public String getEmail() {
      return m_email;
   }

   public long getId() {
      return m_id;
   }

   public long getKeyId() {
      return m_keyId;
   }

   public int getLevel() {
      return m_level;
   }

   public java.util.Date getModifyDate() {
      return m_updateTime;
   }

   public String getOwner() {
      return m_owner;
   }

   public String getPhone() {
      return m_phone;
   }

   public java.util.Date getUpdateTime() {
      return m_updateTime;
   }

   public Project setBu(String bu) {
      m_bu = bu;
      return this;
   }

   public Project setCmdbDomain(String cmdbDomain) {
      m_cmdbDomain = cmdbDomain;
      return this;
   }

   public Project setCmdbProductline(String cmdbProductline) {
      m_cmdbProductline = cmdbProductline;
      return this;
   }

   public Project setCreationDate(java.util.Date creationDate) {
      m_createTime = creationDate;
      return this;
   }

   public Project setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
      return this;
   }

   public Project setDomain(String domain) {
      m_domain = domain;
      return this;
   }

   public Project setEmail(String email) {
      m_email = email;
      return this;
   }

   public Project setId(long id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public Project setKeyId(long keyId) {
      m_keyId = keyId;
      return this;
   }

   public Project setLevel(int level) {
      m_level = level;
      return this;
   }

   public Project setModifyDate(java.util.Date modifyDate) {
      m_updateTime = modifyDate;
      return this;
   }

   public Project setOwner(String owner) {
      m_owner = owner;
      return this;
   }

   public Project setPhone(String phone) {
      m_phone = phone;
      return this;
   }

   public Project setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("Project[");
      sb.append("bu: ").append(m_bu);
      sb.append(", cmdb-domain: ").append(m_cmdbDomain);
      sb.append(", cmdb-productline: ").append(m_cmdbProductline);
      sb.append(", create-time: ").append(m_createTime);
      sb.append(", domain: ").append(m_domain);
      sb.append(", email: ").append(m_email);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", level: ").append(m_level);
      sb.append(", owner: ").append(m_owner);
      sb.append(", phone: ").append(m_phone);
      sb.append(", update-time: ").append(m_updateTime);
      sb.append("]");
      return sb.toString();
   }

}
