package com.dianping.cat.home.heavy.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.heavy.BaseEntity;
import com.dianping.cat.home.heavy.IVisitor;

public class HeavyCall extends BaseEntity<HeavyCall> {
   private Map<String, Url> m_urls = new LinkedHashMap<String, Url>();

   private Map<String, Service> m_services = new LinkedHashMap<String, Service>();

   public HeavyCall() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitHeavyCall(this);
   }

   public HeavyCall addService(Service service) {
      m_services.put(service.getKey(), service);
      return this;
   }

   public HeavyCall addUrl(Url url) {
      m_urls.put(url.getKey(), url);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof HeavyCall) {
         HeavyCall _o = (HeavyCall) obj;

         if (!equals(getUrls(), _o.getUrls())) {
            return false;
         }

         if (!equals(getServices(), _o.getServices())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public Service findService(String key) {
      return m_services.get(key);
   }

   public Url findUrl(String key) {
      return m_urls.get(key);
   }

   public Service findOrCreateService(String key) {
      Service service = m_services.get(key);

      if (service == null) {
         synchronized (m_services) {
            service = m_services.get(key);

            if (service == null) {
               service = new Service(key);
               m_services.put(key, service);
            }
         }
      }

      return service;
   }

   public Url findOrCreateUrl(String key) {
      Url url = m_urls.get(key);

      if (url == null) {
         synchronized (m_urls) {
            url = m_urls.get(key);

            if (url == null) {
               url = new Url(key);
               m_urls.put(key, url);
            }
         }
      }

      return url;
   }

   public Map<String, Service> getServices() {
      return m_services;
   }

   public Map<String, Url> getUrls() {
      return m_urls;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_urls == null ? 0 : m_urls.hashCode());
      hash = hash * 31 + (m_services == null ? 0 : m_services.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(HeavyCall other) {
   }

   public Service removeService(String key) {
      return m_services.remove(key);
   }

   public Url removeUrl(String key) {
      return m_urls.remove(key);
   }

}
