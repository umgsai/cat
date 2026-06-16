package com.dianping.cat.configuration.server.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.configuration.server.BaseEntity;
import com.dianping.cat.configuration.server.IVisitor;

public class ServerConfig extends BaseEntity<ServerConfig> {
   private Map<String, Server> m_servers = new LinkedHashMap<String, Server>();

   public ServerConfig() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitServerConfig(this);
   }

   public ServerConfig addServer(Server server) {
      m_servers.put(server.getId(), server);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ServerConfig) {
         ServerConfig _o = (ServerConfig) obj;

         if (!equals(getServers(), _o.getServers())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public Server findServer(String id) {
      return m_servers.get(id);
   }

   public Map<String, Server> getServers() {
      return m_servers;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_servers == null ? 0 : m_servers.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(ServerConfig other) {
   }

   public Server removeServer(String id) {
      return m_servers.remove(id);
   }

}
