package com.dianping.cat.home.dal.report;


public class Baseline {
   private int m_id;

   private String m_reportName;

   private String m_indexKey;

   private java.util.Date m_reportPeriod;

   private byte[] m_data;

   private java.util.Date m_creationDate;

   private int m_keyId;

   private double[] m_dataInDoubleArray;
   public void afterLoad() {
      m_keyId = m_id;
   }

   public java.util.Date getCreationDate() {
      return m_creationDate;
   }

   public byte[] getData() {
      return m_data;
   }

   public double[] getDataInDoubleArray() {
      return m_dataInDoubleArray;
   }

   public int getId() {
      return m_id;
   }

   public String getIndexKey() {
      return m_indexKey;
   }

   public int getKeyId() {
      return m_keyId;
   }

   public String getReportName() {
      return m_reportName;
   }

   public java.util.Date getReportPeriod() {
      return m_reportPeriod;
   }

   public Baseline setCreationDate(java.util.Date creationDate) {
      m_creationDate = creationDate;
      return this;
   }

   public Baseline setData(byte[] data) {
      m_data = data;
      return this;
   }

   public Baseline setDataInDoubleArray(double[] dataInDoubleArray) {
      m_dataInDoubleArray = dataInDoubleArray;
      return this;
   }

   public Baseline setId(int id) {
      m_id = id;
      m_keyId = id;
      return this;
   }

   public Baseline setIndexKey(String indexKey) {
      m_indexKey = indexKey;
      return this;
   }

   public Baseline setKeyId(int keyId) {
      m_keyId = keyId;
      return this;
   }

   public Baseline setReportName(String reportName) {
      m_reportName = reportName;
      return this;
   }

   public Baseline setReportPeriod(java.util.Date reportPeriod) {
      m_reportPeriod = reportPeriod;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("Baseline[");
      sb.append("creation-date: ").append(m_creationDate);
      sb.append(", data: ").append(m_data == null ? null : java.util.Arrays.asList(m_data));
      sb.append(", dataInDoubleArray: ").append(m_dataInDoubleArray == null ? null : java.util.Arrays.asList(m_dataInDoubleArray));
      sb.append(", id: ").append(m_id);
      sb.append(", index-key: ").append(m_indexKey);
      sb.append(", key-id: ").append(m_keyId);
      sb.append(", report-name: ").append(m_reportName);
      sb.append(", report-period: ").append(m_reportPeriod);
      sb.append("]");
      return sb.toString();
   }

}
