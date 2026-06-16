package com.dianping.cat.configuration.server.entity;

import static com.dianping.cat.configuration.server.Constants.ATTR_ID;
import static com.dianping.cat.configuration.server.Constants.ENTITY_HARFS;

import com.dianping.cat.configuration.server.BaseEntity;
import com.dianping.cat.configuration.server.IVisitor;

public class HarfsConfig extends BaseEntity<HarfsConfig> {
   private String m_id;

   private String m_maxSize = "128M";

   private String m_serverUri;

   private String m_baseDir;

   public HarfsConfig() {
   }

   public HarfsConfig(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitHarfs(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof HarfsConfig) {
         HarfsConfig _o = (HarfsConfig) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public String getBaseDir() {
      return m_baseDir;
   }

   public String getId() {
      return m_id;
   }

   public String getMaxSize() {
      return m_maxSize;
   }

   public String getServerUri() {
      return m_serverUri;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(HarfsConfig other) {
      assertAttributeEquals(other, ENTITY_HARFS, ATTR_ID, m_id, other.getId());

      if (other.getMaxSize() != null) {
         m_maxSize = other.getMaxSize();
      }

      if (other.getServerUri() != null) {
         m_serverUri = other.getServerUri();
      }

      if (other.getBaseDir() != null) {
         m_baseDir = other.getBaseDir();
      }
   }

   public HarfsConfig setBaseDir(String baseDir) {
      m_baseDir = baseDir;
      return this;
   }

   public HarfsConfig setId(String id) {
      m_id = id;
      return this;
   }

   public HarfsConfig setMaxSize(String maxSize) {
      m_maxSize = maxSize;
      return this;
   }

   public HarfsConfig setServerUri(String serverUri) {
      m_serverUri = serverUri;
      return this;
   }

}
