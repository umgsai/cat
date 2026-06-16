package com.dianping.cat.home.dal.report;


public class ConfigModification {
   private int m_id;

   private String m_userName;

   private String m_accountName;

   private String m_actionName;

   private String m_argument;

   private java.util.Date m_date;

   private java.util.Date m_creationDate;

   private int m_keyId;
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
      return m_creationDate;
   }

   public java.util.Date getDate() {
      return m_date;
   }

   public int getId() {
      return m_id;
   }

   public int getKeyId() {
      return m_keyId;
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
      m_creationDate = creationDate;
      return this;
   }

   public ConfigModification setDate(java.util.Date date) {
      m_date = date;
      return this;
   }

   public ConfigModification setId(int id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public ConfigModification setKeyId(int keyId) {
      m_keyId = keyId;
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
      sb.append(", creation-date: ").append(m_creationDate);
      sb.append(", date: ").append(m_date);
      sb.append(", id: ").append(m_id);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", user-name: ").append(m_userName);
      sb.append("]");
      return sb.toString();
   }

}
