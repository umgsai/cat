package com.dianping.cat.home.router.entity;

import static com.dianping.cat.home.router.Constants.ATTR_ID;
import static com.dianping.cat.home.router.Constants.ENTITY_NETWORK;

import com.dianping.cat.home.router.BaseEntity;
import com.dianping.cat.home.router.IVisitor;

public class Network extends BaseEntity<Network> {
   private String m_id;

   public Network() {
   }

   public Network(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitNetwork(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Network) {
         Network _o = (Network) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public String getId() {
      return m_id;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(Network other) {
      assertAttributeEquals(other, ENTITY_NETWORK, ATTR_ID, m_id, other.getId());

   }

   public Network setId(String id) {
      m_id = id;
      return this;
   }

}
