package com.dianping.cat.home.resource.entity;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.home.resource.BaseEntity;
import com.dianping.cat.home.resource.IVisitor;

public class ResourceConfig extends BaseEntity<ResourceConfig> {
   private List<Resource> m_resources = new ArrayList<Resource>();

   public ResourceConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitResourceConfig(this);
   }

   public ResourceConfig addResource(Resource resource) {
      m_resources.add(resource);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ResourceConfig) {
         ResourceConfig _o = (ResourceConfig) obj;

         if (!equals(getResources(), _o.getResources())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public List<Resource> getResources() {
      return m_resources;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      for (Resource e : m_resources) {
         hash = hash * 31 + (e == null ? 0 :e.hashCode());
      }


      return hash;
   }

   @Override
   public void mergeAttributes(ResourceConfig other) {
   }

}
