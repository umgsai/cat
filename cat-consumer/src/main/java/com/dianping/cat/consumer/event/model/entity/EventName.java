package com.dianping.cat.consumer.event.model.entity;

import static com.dianping.cat.consumer.event.model.Constants.ATTR_ID;
import static com.dianping.cat.consumer.event.model.Constants.ENTITY_NAME;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.cat.consumer.event.model.BaseEntity;
import com.dianping.cat.consumer.event.model.IVisitor;

public class EventName extends BaseEntity<EventName> {
   private String m_id;

   private long m_totalCount;

   private long m_failCount;

   private double m_failPercent;

   private String m_successMessageUrl;

   private String m_failMessageUrl;

   private Map<Integer, Range> m_ranges = new ConcurrentHashMap<Integer, Range>();

   private double m_tps;

   private transient double m_totalPercent;

   private GraphTrend m_graphTrend;

   private Map<String, StatusCode> m_statusCodes = new ConcurrentHashMap<String, StatusCode>();

   public EventName() {
   }

   public EventName(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitName(this);
   }

   public EventName addRange(Range range) {
      m_ranges.put(range.getValue(), range);
      return this;
   }

   public EventName addStatusCode(StatusCode statusCode) {
      m_statusCodes.put(statusCode.getId(), statusCode);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof EventName) {
         EventName _o = (EventName) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public Range findRange(Integer value) {
      return m_ranges.get(value);
   }

   public StatusCode findStatusCode(String id) {
      return m_statusCodes.get(id);
   }

   public Range findOrCreateRange(Integer value) {
      Range range = m_ranges.get(value);

      if (range == null) {
         synchronized (m_ranges) {
            range = m_ranges.get(value);

            if (range == null) {
               range = new Range(value);
               m_ranges.put(value, range);
            }
         }
      }

      return range;
   }

   public StatusCode findOrCreateStatusCode(String id) {
      StatusCode statusCode = m_statusCodes.get(id);

      if (statusCode == null) {
         synchronized (m_statusCodes) {
            statusCode = m_statusCodes.get(id);

            if (statusCode == null) {
               statusCode = new StatusCode(id);
               m_statusCodes.put(id, statusCode);
            }
         }
      }

      return statusCode;
   }

   public long getFailCount() {
      return m_failCount;
   }

   public String getFailMessageUrl() {
      return m_failMessageUrl;
   }

   public double getFailPercent() {
      return m_failPercent;
   }

   public GraphTrend getGraphTrend() {
      return m_graphTrend;
   }

   public String getId() {
      return m_id;
   }

   public Map<Integer, Range> getRanges() {
      return m_ranges;
   }

   public Map<String, StatusCode> getStatusCodes() {
      return m_statusCodes;
   }

   public String getSuccessMessageUrl() {
      return m_successMessageUrl;
   }

   public long getTotalCount() {
      return m_totalCount;
   }

   public double getTotalPercent() {
      return m_totalPercent;
   }

   public double getTps() {
      return m_tps;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_id == null ? 0 : m_id.hashCode());

      return hash;
   }

   public EventName incFailCount() {
      m_failCount++;
      return this;
   }

   public EventName incFailCount(long failCount) {
      m_failCount += failCount;
      return this;
   }

   public EventName incTotalCount() {
      m_totalCount++;
      return this;
   }

   public EventName incTotalCount(long totalCount) {
      m_totalCount += totalCount;
      return this;
   }

   @Override
   public void mergeAttributes(EventName other) {
      assertAttributeEquals(other, ENTITY_NAME, ATTR_ID, m_id, other.getId());

      m_totalCount = other.getTotalCount();

      m_failCount = other.getFailCount();

      m_failPercent = other.getFailPercent();

      m_tps = other.getTps();

      m_totalPercent = other.getTotalPercent();
   }

   public Range removeRange(Integer value) {
      return m_ranges.remove(value);
   }

   public StatusCode removeStatusCode(String id) {
      return m_statusCodes.remove(id);
   }

   public EventName setFailCount(long failCount) {
      m_failCount = failCount;
      return this;
   }

   public EventName setFailMessageUrl(String failMessageUrl) {
      m_failMessageUrl = failMessageUrl;
      return this;
   }

   public EventName setFailPercent(double failPercent) {
      m_failPercent = failPercent;
      return this;
   }

   public EventName setGraphTrend(GraphTrend graphTrend) {
      m_graphTrend = graphTrend;
      return this;
   }

   public EventName setId(String id) {
      m_id = id;
      return this;
   }

   public EventName setSuccessMessageUrl(String successMessageUrl) {
      m_successMessageUrl = successMessageUrl;
      return this;
   }

   public EventName setTotalCount(long totalCount) {
      m_totalCount = totalCount;
      return this;
   }

   public EventName setTotalPercent(double totalPercent) {
      m_totalPercent = totalPercent;
      return this;
   }

   public EventName setTps(double tps) {
      m_tps = tps;
      return this;
   }

}
