package com.dianping.cat.home.router.entity;

import static com.dianping.cat.home.router.Constants.ATTR_DOMAIN;
import static com.dianping.cat.home.router.Constants.ENTITY_ROUTER_CONFIG;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.router.BaseEntity;
import com.dianping.cat.home.router.IVisitor;

public class RouterConfig extends BaseEntity<RouterConfig> {
   private String m_backupServer;

   private int m_backupServerPort;

   private Map<String, DefaultServer> m_defaultServers = new LinkedHashMap<String, DefaultServer>();

   private Map<String, NetworkPolicy> m_networkPolicies = new LinkedHashMap<String, NetworkPolicy>();

   private Map<String, ServerGroup> m_serverGroups = new LinkedHashMap<String, ServerGroup>();

   private Map<String, Domain> m_domains = new LinkedHashMap<String, Domain>();

   private java.util.Date m_startTime;

   private String m_domain;

   private java.util.Date m_endTime;

   public RouterConfig() {
   }

   public RouterConfig(String domain) {
      m_domain = domain;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitRouterConfig(this);
   }

   public RouterConfig addDefaultServer(DefaultServer defaultServer) {
      m_defaultServers.put(defaultServer.getId(), defaultServer);
      return this;
   }

   public RouterConfig addDomain(Domain domain) {
      m_domains.put(domain.getId(), domain);
      return this;
   }

   public RouterConfig addNetworkPolicy(NetworkPolicy networkPolicy) {
      m_networkPolicies.put(networkPolicy.getId(), networkPolicy);
      return this;
   }

   public RouterConfig addServerGroup(ServerGroup serverGroup) {
      m_serverGroups.put(serverGroup.getId(), serverGroup);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof RouterConfig) {
         RouterConfig _o = (RouterConfig) obj;

         if (!equals(getDomain(), _o.getDomain())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public DefaultServer findDefaultServer(String id) {
      return m_defaultServers.get(id);
   }

   public Domain findDomain(String id) {
      return m_domains.get(id);
   }

   public NetworkPolicy findNetworkPolicy(String id) {
      return m_networkPolicies.get(id);
   }

   public ServerGroup findServerGroup(String id) {
      return m_serverGroups.get(id);
   }

   public Domain findOrCreateDomain(String id) {
      Domain domain = m_domains.get(id);

      if (domain == null) {
         synchronized (m_domains) {
            domain = m_domains.get(id);

            if (domain == null) {
               domain = new Domain(id);
               m_domains.put(id, domain);
            }
         }
      }

      return domain;
   }

   public NetworkPolicy findOrCreateNetworkPolicy(String id) {
      NetworkPolicy networkPolicy = m_networkPolicies.get(id);

      if (networkPolicy == null) {
         synchronized (m_networkPolicies) {
            networkPolicy = m_networkPolicies.get(id);

            if (networkPolicy == null) {
               networkPolicy = new NetworkPolicy(id);
               m_networkPolicies.put(id, networkPolicy);
            }
         }
      }

      return networkPolicy;
   }

   public ServerGroup findOrCreateServerGroup(String id) {
      ServerGroup serverGroup = m_serverGroups.get(id);

      if (serverGroup == null) {
         synchronized (m_serverGroups) {
            serverGroup = m_serverGroups.get(id);

            if (serverGroup == null) {
               serverGroup = new ServerGroup(id);
               m_serverGroups.put(id, serverGroup);
            }
         }
      }

      return serverGroup;
   }

   public String getBackupServer() {
      return m_backupServer;
   }

   public int getBackupServerPort() {
      return m_backupServerPort;
   }

   public Map<String, DefaultServer> getDefaultServers() {
      return m_defaultServers;
   }

   public String getDomain() {
      return m_domain;
   }

   public Map<String, Domain> getDomains() {
      return m_domains;
   }

   public java.util.Date getEndTime() {
      return m_endTime;
   }

   public Map<String, NetworkPolicy> getNetworkPolicies() {
      return m_networkPolicies;
   }

   public Map<String, ServerGroup> getServerGroups() {
      return m_serverGroups;
   }

   public java.util.Date getStartTime() {
      return m_startTime;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_domain == null ? 0 : m_domain.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(RouterConfig other) {
      assertAttributeEquals(other, ENTITY_ROUTER_CONFIG, ATTR_DOMAIN, m_domain, other.getDomain());

      if (other.getBackupServer() != null) {
         m_backupServer = other.getBackupServer();
      }

      m_backupServerPort = other.getBackupServerPort();

      if (other.getStartTime() != null) {
         m_startTime = other.getStartTime();
      }

      if (other.getEndTime() != null) {
         m_endTime = other.getEndTime();
      }
   }

   public DefaultServer removeDefaultServer(String id) {
      return m_defaultServers.remove(id);
   }

   public Domain removeDomain(String id) {
      return m_domains.remove(id);
   }

   public NetworkPolicy removeNetworkPolicy(String id) {
      return m_networkPolicies.remove(id);
   }

   public ServerGroup removeServerGroup(String id) {
      return m_serverGroups.remove(id);
   }

   public RouterConfig setBackupServer(String backupServer) {
      m_backupServer = backupServer;
      return this;
   }

   public RouterConfig setBackupServerPort(int backupServerPort) {
      m_backupServerPort = backupServerPort;
      return this;
   }

   public RouterConfig setDomain(String domain) {
      m_domain = domain;
      return this;
   }

   public RouterConfig setEndTime(java.util.Date endTime) {
      m_endTime = endTime;
      return this;
   }

   public RouterConfig setStartTime(java.util.Date startTime) {
      m_startTime = startTime;
      return this;
   }

}
