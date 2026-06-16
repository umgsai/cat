package com.dianping.cat.consumer.transaction.model.entity;

import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_ID;
import static com.dianping.cat.consumer.transaction.model.Constants.ENTITY_STATUSCODE;

import com.dianping.cat.consumer.transaction.model.BaseEntity;
import com.dianping.cat.consumer.transaction.model.IVisitor;

public class StatusCode extends BaseEntity<StatusCode> {
   private String m_id;

   private long m_count;

   public StatusCode() {
   }

   public StatusCode(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitStatusCode(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof StatusCode) {
         StatusCode _o = (StatusCode) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public long getCount() {
      return m_count;
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

   public StatusCode incCount() {
      m_count++;
      return this;
   }

   public StatusCode incCount(long count) {
      m_count += count;
      return this;
   }

   @Override
   public void mergeAttributes(StatusCode other) {
      assertAttributeEquals(other, ENTITY_STATUSCODE, ATTR_ID, m_id, other.getId());

      m_count = other.getCount();
   }

   public StatusCode setCount(long count) {
      m_count = count;
      return this;
   }

   public StatusCode setId(String id) {
      m_id = id;
      return this;
   }

}
