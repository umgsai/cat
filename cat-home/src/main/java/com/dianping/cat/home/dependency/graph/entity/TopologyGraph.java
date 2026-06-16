package com.dianping.cat.home.dependency.graph.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.home.dependency.graph.BaseEntity;
import com.dianping.cat.home.dependency.graph.IVisitor;

public class TopologyGraph extends BaseEntity<TopologyGraph> {
   private String m_id;

   private String m_type;

   private int m_status;

   private String m_des;

   private Map<String, TopologyNode> m_nodes = new LinkedHashMap<String, TopologyNode>();

   private Map<String, TopologyEdge> m_edges = new LinkedHashMap<String, TopologyEdge>();

   public TopologyGraph() {
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitTopologyGraph(this);
   }

   public TopologyGraph addTopologyEdge(TopologyEdge topologyEdge) {
      m_edges.put(topologyEdge.getKey(), topologyEdge);
      return this;
   }

   public TopologyGraph addTopologyNode(TopologyNode topologyNode) {
      m_nodes.put(topologyNode.getId(), topologyNode);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof TopologyGraph) {
         TopologyGraph _o = (TopologyGraph) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         if (!equals(getType(), _o.getType())) {
            return false;
         }

         if (getStatus() != _o.getStatus()) {
            return false;
         }

         if (!equals(getDes(), _o.getDes())) {
            return false;
         }

         if (!equals(getNodes(), _o.getNodes())) {
            return false;
         }

         if (!equals(getEdges(), _o.getEdges())) {
            return false;
         }


         return true;
      }

      return false;
   }

   public TopologyEdge findTopologyEdge(String key) {
      return m_edges.get(key);
   }

   public TopologyNode findTopologyNode(String id) {
      return m_nodes.get(id);
   }

   public TopologyEdge findOrCreateTopologyEdge(String key) {
      TopologyEdge topologyEdge = m_edges.get(key);

      if (topologyEdge == null) {
         synchronized (m_edges) {
            topologyEdge = m_edges.get(key);

            if (topologyEdge == null) {
               topologyEdge = new TopologyEdge(key);
               m_edges.put(key, topologyEdge);
            }
         }
      }

      return topologyEdge;
   }

   public TopologyNode findOrCreateTopologyNode(String id) {
      TopologyNode topologyNode = m_nodes.get(id);

      if (topologyNode == null) {
         synchronized (m_nodes) {
            topologyNode = m_nodes.get(id);

            if (topologyNode == null) {
               topologyNode = new TopologyNode(id);
               m_nodes.put(id, topologyNode);
            }
         }
      }

      return topologyNode;
   }

   public String getDes() {
      return m_des;
   }

   public Map<String, TopologyEdge> getEdges() {
      return m_edges;
   }

   public String getId() {
      return m_id;
   }

   public Map<String, TopologyNode> getNodes() {
      return m_nodes;
   }

   public int getStatus() {
      return m_status;
   }

   public String getType() {
      return m_type;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());
      hash = hash * 31 + (m_type == null ? 0 : m_type.hashCode());
      hash = hash * 31 + m_status;
      hash = hash * 31 + (m_des == null ? 0 : m_des.hashCode());
      hash = hash * 31 + (m_nodes == null ? 0 : m_nodes.hashCode());
      hash = hash * 31 + (m_edges == null ? 0 : m_edges.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(TopologyGraph other) {
      if (other.getId() != null) {
         m_id = other.getId();
      }

      if (other.getType() != null) {
         m_type = other.getType();
      }

      m_status = other.getStatus();

      if (other.getDes() != null) {
         m_des = other.getDes();
      }
   }

   public TopologyEdge removeTopologyEdge(String key) {
      return m_edges.remove(key);
   }

   public TopologyNode removeTopologyNode(String id) {
      return m_nodes.remove(id);
   }

   public TopologyGraph setDes(String des) {
      m_des = des;
      return this;
   }

   public TopologyGraph setId(String id) {
      m_id = id;
      return this;
   }

   public TopologyGraph setStatus(int status) {
      m_status = status;
      return this;
   }

   public TopologyGraph setType(String type) {
      m_type = type;
      return this;
   }

}
