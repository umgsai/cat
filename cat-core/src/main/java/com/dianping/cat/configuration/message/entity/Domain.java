package com.dianping.cat.configuration.message.entity;

import static com.dianping.cat.configuration.message.Constants.ATTR_ID;
import static com.dianping.cat.configuration.message.Constants.ENTITY_DOMAIN;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.configuration.message.BaseEntity;
import com.dianping.cat.configuration.message.IVisitor;

public class Domain extends BaseEntity<Domain> {
   private String m_id;

   private String m_startTypes;

   private String m_matchTypes;

   private Map<String, Property> m_properties = new LinkedHashMap<String, Property>();

   public Domain() {
   }

   public Domain(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitDomain(this);
   }

   public Domain addProperty(Property property) {
      m_properties.put(property.getName(), property);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Domain) {
         Domain _o = (Domain) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public Property findProperty(String name) {
      return m_properties.get(name);
   }

   public String getId() {
      return m_id;
   }

   public String getMatchTypes() {
      return m_matchTypes;
   }

   public Map<String, Property> getProperties() {
      return m_properties;
   }

   public String getStartTypes() {
      return m_startTypes;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(Domain other) {
      assertAttributeEquals(other, ENTITY_DOMAIN, ATTR_ID, m_id, other.getId());

   }

   public Property removeProperty(String name) {
      return m_properties.remove(name);
   }

   public Domain setId(String id) {
      m_id = id;
      return this;
   }

   public Domain setMatchTypes(String matchTypes) {
      m_matchTypes = matchTypes;
      return this;
   }

   public Domain setStartTypes(String startTypes) {
      m_startTypes = startTypes;
      return this;
   }

}
