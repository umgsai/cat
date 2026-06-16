package com.dianping.cat.home.resource.entity;

import com.dianping.cat.home.resource.BaseEntity;
import com.dianping.cat.home.resource.IVisitor;

public class Resource extends BaseEntity<Resource> {
   private String m_path;

   private String m_op;

   private int m_role;

   public Resource() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitResource(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Resource) {
         Resource _o = (Resource) obj;

         if (!equals(getPath(), _o.getPath())) {
            return false;
         }

         if (!equals(getOp(), _o.getOp())) {
            return false;
         }

         if (getRole() != _o.getRole()) {
            return false;
         }


         return true;
      }

      return false;
   }

   public String getOp() {
      return m_op;
   }

   public String getPath() {
      return m_path;
   }

   public int getRole() {
      return m_role;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_path == null ? 0 : m_path.hashCode());
      hash = hash * 31 + (m_op == null ? 0 : m_op.hashCode());
      hash = hash * 31 + m_role;

      return hash;
   }

   @Override
   public void mergeAttributes(Resource other) {
      if (other.getPath() != null) {
         m_path = other.getPath();
      }

      if (other.getOp() != null) {
         m_op = other.getOp();
      }

      m_role = other.getRole();
   }

   public Resource setOp(String op) {
      m_op = op;
      return this;
   }

   public Resource setPath(String path) {
      m_path = path;
      return this;
   }

   public Resource setRole(int role) {
      m_role = role;
      return this;
   }

}
