package com.dianping.cat.home.router.entity;

import static com.dianping.cat.home.router.Constants.ATTR_ID;
import static com.dianping.cat.home.router.Constants.ENTITY_NETWORK_POLICY;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.router.BaseEntity;
import com.dianping.cat.home.router.IVisitor;

public class NetworkPolicy extends BaseEntity<NetworkPolicy> {
   private String m_id;

   private String m_title;

   private boolean m_block = false;

   private String m_serverGroup;

   private Map<String, Network> m_networks = new LinkedHashMap<String, Network>();

   public NetworkPolicy() {
   }

   public NetworkPolicy(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitNetworkPolicy(this);
   }

   public NetworkPolicy addNetwork(Network network) {
      m_networks.put(network.getId(), network);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof NetworkPolicy) {
         NetworkPolicy _o = (NetworkPolicy) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public Network findNetwork(String id) {
      return m_networks.get(id);
   }

   public Network findOrCreateNetwork(String id) {
      Network network = m_networks.get(id);

      if (network == null) {
         synchronized (m_networks) {
            network = m_networks.get(id);

            if (network == null) {
               network = new Network(id);
               m_networks.put(id, network);
            }
         }
      }

      return network;
   }

   public boolean getBlock() {
      return m_block;
   }

   public String getId() {
      return m_id;
   }

   public Map<String, Network> getNetworks() {
      return m_networks;
   }

   public String getServerGroup() {
      return m_serverGroup;
   }

   public String getTitle() {
      return m_title;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   public boolean isBlock() {
      return m_block;
   }

   @Override
   public void mergeAttributes(NetworkPolicy other) {
      assertAttributeEquals(other, ENTITY_NETWORK_POLICY, ATTR_ID, m_id, other.getId());

      if (other.getTitle() != null) {
         m_title = other.getTitle();
      }

      m_block = other.getBlock();

      if (other.getServerGroup() != null) {
         m_serverGroup = other.getServerGroup();
      }
   }

   public Network removeNetwork(String id) {
      return m_networks.remove(id);
   }

   public NetworkPolicy setBlock(boolean block) {
      m_block = block;
      return this;
   }

   public NetworkPolicy setId(String id) {
      m_id = id;
      return this;
   }

   public NetworkPolicy setServerGroup(String serverGroup) {
      m_serverGroup = serverGroup;
      return this;
   }

   public NetworkPolicy setTitle(String title) {
      m_title = title;
      return this;
   }

}
