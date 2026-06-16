package com.dianping.cat.consumer.transaction.model.entity;

import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_ID;
import static com.dianping.cat.consumer.transaction.model.Constants.ENTITY_NAME;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.cat.consumer.transaction.model.BaseEntity;
import com.dianping.cat.consumer.transaction.model.IVisitor;

public class TransactionName extends BaseEntity<TransactionName> {
   private String m_id;

   private long m_totalCount;

   private long m_failCount;

   private double m_failPercent;

   private double m_min = 86400000d;

   private double m_max = -1d;

   private double m_avg;

   private double m_sum;

   private double m_sum2;

   private double m_std;

   private String m_successMessageUrl;

   private String m_failMessageUrl;

   private Map<Integer, Range> m_ranges = new ConcurrentHashMap<Integer, Range>();

   private Map<Integer, Duration> m_durations = new ConcurrentHashMap<Integer, Duration>();

   private transient double m_totalPercent;

   private double m_tps;

   private double m_line95Value;

   private double m_line99Value;

   private double m_line999Value;

   private double m_line90Value;

   private Map<Integer, Graph> m_graphs = new ConcurrentHashMap<Integer, Graph>();

   private transient Map<Integer, AllDuration> m_allDurations = new ConcurrentHashMap<Integer, AllDuration>();

   private GraphTrend m_graphTrend;

   private Map<String, StatusCode> m_statusCodes = new ConcurrentHashMap<String, StatusCode>();

   private double m_line50Value;

   private double m_line9999Value;

   private String m_longestMessageUrl;

   public TransactionName() {
   }

   public TransactionName(String id) {
      m_id = id;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitName(this);
   }

   public TransactionName addAllDuration(AllDuration allDuration) {
      m_allDurations.put(allDuration.getValue(), allDuration);
      return this;
   }

   public TransactionName addDuration(Duration duration) {
      m_durations.put(duration.getValue(), duration);
      return this;
   }

   public TransactionName addGraph(Graph graph) {
      m_graphs.put(graph.getDuration(), graph);
      return this;
   }

   public TransactionName addRange(Range range) {
      m_ranges.put(range.getValue(), range);
      return this;
   }

   public TransactionName addStatusCode(StatusCode statusCode) {
      m_statusCodes.put(statusCode.getId(), statusCode);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof TransactionName) {
         TransactionName _o = (TransactionName) obj;

         if (!equals(getId(), _o.getId())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public AllDuration findAllDuration(int value) {
      return m_allDurations.get(value);
   }

   public Duration findDuration(int value) {
      return m_durations.get(value);
   }

   public Graph findGraph(int duration) {
      return m_graphs.get(duration);
   }

   public Range findRange(int value) {
      return m_ranges.get(value);
   }

   public StatusCode findStatusCode(String id) {
      return m_statusCodes.get(id);
   }

   public AllDuration findOrCreateAllDuration(int value) {
      AllDuration allDuration = m_allDurations.get(value);

      if (allDuration == null) {
         synchronized (m_allDurations) {
            allDuration = m_allDurations.get(value);

            if (allDuration == null) {
               allDuration = new AllDuration(value);
               m_allDurations.put(value, allDuration);
            }
         }
      }

      return allDuration;
   }

   public Duration findOrCreateDuration(int value) {
      Duration duration = m_durations.get(value);

      if (duration == null) {
         synchronized (m_durations) {
            duration = m_durations.get(value);

            if (duration == null) {
               duration = new Duration(value);
               m_durations.put(value, duration);
            }
         }
      }

      return duration;
   }

   public Graph findOrCreateGraph(int duration) {
      Graph graph = m_graphs.get(duration);

      if (graph == null) {
         synchronized (m_graphs) {
            graph = m_graphs.get(duration);

            if (graph == null) {
               graph = new Graph(duration);
               m_graphs.put(duration, graph);
            }
         }
      }

      return graph;
   }

   public Range findOrCreateRange(int value) {
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

   public Map<Integer, AllDuration> getAllDurations() {
      return m_allDurations;
   }

   public double getAvg() {
      return m_avg;
   }

   public Map<Integer, Duration> getDurations() {
      return m_durations;
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

   public Map<Integer, Graph> getGraphs() {
      return m_graphs;
   }

   public GraphTrend getGraphTrend() {
      return m_graphTrend;
   }

   public String getId() {
      return m_id;
   }

   public double getLine50Value() {
      return m_line50Value;
   }

   public double getLine90Value() {
      return m_line90Value;
   }

   public double getLine95Value() {
      return m_line95Value;
   }

   public double getLine9999Value() {
      return m_line9999Value;
   }

   public double getLine999Value() {
      return m_line999Value;
   }

   public double getLine99Value() {
      return m_line99Value;
   }

   public String getLongestMessageUrl() {
      return m_longestMessageUrl;
   }

   public double getMax() {
      return m_max;
   }

   public double getMin() {
      return m_min;
   }

   public Map<Integer, Range> getRanges() {
      return m_ranges;
   }

   public Map<String, StatusCode> getStatusCodes() {
      return m_statusCodes;
   }

   public double getStd() {
      return m_std;
   }

   public String getSuccessMessageUrl() {
      return m_successMessageUrl;
   }

   public double getSum() {
      return m_sum;
   }

   public double getSum2() {
      return m_sum2;
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

   public TransactionName incFailCount() {
      m_failCount++;
      return this;
   }

   public TransactionName incFailCount(long failCount) {
      m_failCount += failCount;
      return this;
   }

   public TransactionName incTotalCount() {
      m_totalCount++;
      return this;
   }

   public TransactionName incTotalCount(long totalCount) {
      m_totalCount += totalCount;
      return this;
   }

   @Override
   public void mergeAttributes(TransactionName other) {
      assertAttributeEquals(other, ENTITY_NAME, ATTR_ID, m_id, other.getId());

      m_totalCount = other.getTotalCount();

      m_failCount = other.getFailCount();

      m_failPercent = other.getFailPercent();

      m_min = other.getMin();

      m_max = other.getMax();

      m_avg = other.getAvg();

      m_sum = other.getSum();

      m_sum2 = other.getSum2();

      m_std = other.getStd();

      m_totalPercent = other.getTotalPercent();

      m_tps = other.getTps();

      m_line95Value = other.getLine95Value();

      m_line99Value = other.getLine99Value();

      m_line999Value = other.getLine999Value();

      m_line90Value = other.getLine90Value();

      m_line50Value = other.getLine50Value();

      m_line9999Value = other.getLine9999Value();
   }

   public AllDuration removeAllDuration(int value) {
      return m_allDurations.remove(value);
   }

   public Duration removeDuration(int value) {
      return m_durations.remove(value);
   }

   public Graph removeGraph(int duration) {
      return m_graphs.remove(duration);
   }

   public Range removeRange(int value) {
      return m_ranges.remove(value);
   }

   public StatusCode removeStatusCode(String id) {
      return m_statusCodes.remove(id);
   }

   public TransactionName setAvg(double avg) {
      m_avg = avg;
      return this;
   }

   public TransactionName setFailCount(long failCount) {
      m_failCount = failCount;
      return this;
   }

   public TransactionName setFailMessageUrl(String failMessageUrl) {
      m_failMessageUrl = failMessageUrl;
      return this;
   }

   public TransactionName setFailPercent(double failPercent) {
      m_failPercent = failPercent;
      return this;
   }

   public TransactionName setGraphTrend(GraphTrend graphTrend) {
      m_graphTrend = graphTrend;
      return this;
   }

   public TransactionName setId(String id) {
      m_id = id;
      return this;
   }

   public TransactionName setLine50Value(double line50Value) {
      m_line50Value = line50Value;
      return this;
   }

   public TransactionName setLine90Value(double line90Value) {
      m_line90Value = line90Value;
      return this;
   }

   public TransactionName setLine95Value(double line95Value) {
      m_line95Value = line95Value;
      return this;
   }

   public TransactionName setLine9999Value(double line9999Value) {
      m_line9999Value = line9999Value;
      return this;
   }

   public TransactionName setLine999Value(double line999Value) {
      m_line999Value = line999Value;
      return this;
   }

   public TransactionName setLine99Value(double line99Value) {
      m_line99Value = line99Value;
      return this;
   }

   public TransactionName setLongestMessageUrl(String longestMessageUrl) {
      m_longestMessageUrl = longestMessageUrl;
      return this;
   }

   public TransactionName setMax(double max) {
      m_max = max;
      return this;
   }

   public TransactionName setMin(double min) {
      m_min = min;
      return this;
   }

   public TransactionName setStd(double std) {
      m_std = std;
      return this;
   }

   public TransactionName setSuccessMessageUrl(String successMessageUrl) {
      m_successMessageUrl = successMessageUrl;
      return this;
   }

   public TransactionName setSum(double sum) {
      m_sum = sum;
      return this;
   }

   public TransactionName setSum2(double sum2) {
      m_sum2 = sum2;
      return this;
   }

   public TransactionName setTotalCount(long totalCount) {
      m_totalCount = totalCount;
      return this;
   }

   public TransactionName setTotalPercent(double totalPercent) {
      m_totalPercent = totalPercent;
      return this;
   }

   public TransactionName setTps(double tps) {
      m_tps = tps;
      return this;
   }

}
