package com.dianping.cat.home.dal.report;


public class ConfigModification {
   private long m_id;

   private String m_userName;

   private String m_accountName;

   private String m_actionName;

   private String m_argument;

   private java.util.Date m_modifyTime;

   private java.util.Date m_createTime;

   private java.util.Date m_updateTime;

   private long m_keyId;
   public void afterLoad() {
      m_keyId = m_id;
   }

   public String getAccountName() {
      return m_accountName;
   }

   public String getActionName() {
      return m_actionName;
   }

   public String getArgument() {
      return m_argument;
   }

   public java.util.Date getCreationDate() {
      return m_createTime;
   }

   public java.util.Date getCreateTime() {
      return m_createTime;
   }

   public java.util.Date getDate() {
      return m_modifyTime;
   }

   public long getId() {
      return m_id;
   }

   public long getKeyId() {
      return m_keyId;
   }

   public java.util.Date getModifyTime() {
      return m_modifyTime;
   }

   public java.util.Date getUpdateTime() {
      return m_updateTime;
   }

   public String getUserName() {
      return m_userName;
   }

   public ConfigModification setAccountName(String accountName) {
      m_accountName = accountName;
      return this;
   }

   public ConfigModification setActionName(String actionName) {
      m_actionName = actionName;
      return this;
   }

   public ConfigModification setArgument(String argument) {
      m_argument = argument;
      return this;
   }

   public ConfigModification setCreationDate(java.util.Date creationDate) {
      m_createTime = creationDate;
      return this;
   }

   public ConfigModification setCreateTime(java.util.Date createTime) {
      m_createTime = createTime;
      return this;
   }

   public ConfigModification setDate(java.util.Date date) {
      m_modifyTime = date;
      return this;
   }

   public ConfigModification setId(int id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public ConfigModification setId(long id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public ConfigModification setKeyId(int keyId) {
      m_keyId = keyId;
      return this;
   }

   public ConfigModification setKeyId(long keyId) {
      m_keyId = keyId;
      return this;
   }

   public ConfigModification setModifyTime(java.util.Date modifyTime) {
      m_modifyTime = modifyTime;
      return this;
   }

   public ConfigModification setUpdateTime(java.util.Date updateTime) {
      m_updateTime = updateTime;
      return this;
   }

   public ConfigModification setUserName(String userName) {
      m_userName = userName;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("ConfigModification[");
      sb.append("account-name: ").append(m_accountName);
      sb.append(", action-name: ").append(m_actionName);
      sb.append(", argument: ").append(m_argument);
      sb.append(", create-time: ").append(m_createTime);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", modify-time: ").append(m_modifyTime);
      sb.append(", update-time: ").append(m_updateTime);
      sb.append(", user-name: ").append(m_userName);
      sb.append("]");
      return sb.toString();
   }

}
