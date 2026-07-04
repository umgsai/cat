package com.dianping.cat.consumer.heartbeat.model.entity;

import static com.dianping.cat.consumer.heartbeat.model.Constants.ATTR_IP;
import static com.dianping.cat.consumer.heartbeat.model.Constants.ENTITY_MACHINE;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.heartbeat.model.BaseEntity;
import com.dianping.cat.consumer.heartbeat.model.IVisitor;

public class Machine extends BaseEntity<Machine> {
   private String m_ip;

   private List<Period> m_periods = new ArrayList<Period>();

   private String m_classpath;

   private String m_javaVersion;

   private String m_userDir;

   private String m_userName;

   public Machine() {
   }

   public Machine(String ip) {
      m_ip = ip;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitMachine(this);
   }

   public Machine addPeriod(Period period) {
      m_periods.add(period);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Machine) {
         Machine _o = (Machine) obj;

         if (!equals(getIp(), _o.getIp())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public Period findPeriod(int minute) {
      for (Period period : m_periods) {
         if (period.getMinute() != minute) {
            continue;
         }

         return period;
      }

      return null;
   }

   public String getClasspath() {
      return m_classpath;
   }

   public String getIp() {
      return m_ip;
   }

   public String getJavaVersion() {
      return m_javaVersion;
   }

   public List<Period> getPeriods() {
      return m_periods;
   }

   public String getUserDir() {
      return m_userDir;
   }

   public String getUserName() {
      return m_userName;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_ip == null ? 0 : m_ip.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(Machine other) {
      assertAttributeEquals(other, ENTITY_MACHINE, ATTR_IP, m_ip, other.getIp());

      if (other.getClasspath() != null) {
         m_classpath = other.getClasspath();
      }

      if (other.getJavaVersion() != null) {
         m_javaVersion = other.getJavaVersion();
      }

      if (other.getUserDir() != null) {
         m_userDir = other.getUserDir();
      }

      if (other.getUserName() != null) {
         m_userName = other.getUserName();
      }
   }

   public Period removePeriod(int minute) {
      int len = m_periods.size();

      for (int i = 0; i < len; i++) {
         Period period = m_periods.get(i);

         if (period.getMinute() != minute) {
            continue;
         }

         return m_periods.remove(i);
      }

      return null;
   }

   public Machine setClasspath(String classpath) {
      m_classpath = classpath;
      return this;
   }

   public Machine setIp(String ip) {
      m_ip = ip;
      return this;
   }

   public Machine setJavaVersion(String javaVersion) {
      m_javaVersion = javaVersion;
      return this;
   }

   public Machine setUserDir(String userDir) {
      m_userDir = userDir;
      return this;
   }

   public Machine setUserName(String userName) {
      m_userName = userName;
      return this;
   }

}
