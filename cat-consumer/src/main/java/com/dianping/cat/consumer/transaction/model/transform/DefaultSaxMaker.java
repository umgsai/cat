package com.dianping.cat.consumer.transaction.model.transform;

import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_AVG;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_COUNT;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_DOMAIN;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_DURATION;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_ENDTIME;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_FAILCOUNT;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_FAILPERCENT;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_FAILS;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_ID;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_IP;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_LINE50VALUE;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_LINE90VALUE;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_LINE95VALUE;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_LINE9999VALUE;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_LINE999VALUE;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_LINE99VALUE;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_MAX;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_MIN;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_STARTTIME;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_STD;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_SUM;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_SUM2;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_TOTALCOUNT;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_TPS;
import static com.dianping.cat.consumer.transaction.model.Constants.ATTR_VALUE;

import java.util.Map;
import org.xml.sax.Attributes;

import com.dianping.cat.consumer.transaction.model.entity.AllDuration;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Graph;
import com.dianping.cat.consumer.transaction.model.entity.Graph2;
import com.dianping.cat.consumer.transaction.model.entity.GraphTrend;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.Range2;
import com.dianping.cat.consumer.transaction.model.entity.StatusCode;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;

public class DefaultSaxMaker implements IMaker<Attributes> {

   @Override
   public AllDuration buildAllDuration(Attributes attributes) {
      String value = attributes.getValue(ATTR_VALUE);
      String count = attributes.getValue(ATTR_COUNT);
      AllDuration allDuration = new AllDuration(value == null ? 0 : convert(Integer.class, value, 0));

      if (count != null) {
         allDuration.setCount(convert(Integer.class, count, 0));
      }

      return allDuration;
   }

   @Override
   public String buildDomain(Attributes attributes) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Duration buildDuration(Attributes attributes) {
      String value = attributes.getValue(ATTR_VALUE);
      String count = attributes.getValue(ATTR_COUNT);
      Duration duration = new Duration(value == null ? 0 : convert(Integer.class, value, 0));

      if (count != null) {
         duration.setCount(convert(Integer.class, count, 0));
      }

      return duration;
   }

   @Override
   public Graph buildGraph(Attributes attributes) {
      String duration = attributes.getValue(ATTR_DURATION);
      String sum = attributes.getValue(ATTR_SUM);
      String avg = attributes.getValue(ATTR_AVG);
      String count = attributes.getValue(ATTR_COUNT);
      String fails = attributes.getValue(ATTR_FAILS);
      Graph graph = new Graph(duration == null ? 0 : convert(Integer.class, duration, 0));

      if (sum != null) {
         graph.setSum(sum);
      }

      if (avg != null) {
         graph.setAvg(avg);
      }

      if (count != null) {
         graph.setCount(count);
      }

      if (fails != null) {
         graph.setFails(fails);
      }

      return graph;
   }

   @Override
   public Graph2 buildGraph2(Attributes attributes) {
      String duration = attributes.getValue(ATTR_DURATION);
      String sum = attributes.getValue(ATTR_SUM);
      String avg = attributes.getValue(ATTR_AVG);
      String count = attributes.getValue(ATTR_COUNT);
      String fails = attributes.getValue(ATTR_FAILS);
      Graph2 graph2 = new Graph2(duration == null ? 0 : convert(Integer.class, duration, 0));

      if (sum != null) {
         graph2.setSum(sum);
      }

      if (avg != null) {
         graph2.setAvg(avg);
      }

      if (count != null) {
         graph2.setCount(count);
      }

      if (fails != null) {
         graph2.setFails(fails);
      }

      return graph2;
   }

   @Override
   public GraphTrend buildGraphTrend(Attributes attributes) {
      String duration = attributes.getValue(ATTR_DURATION);
      String sum = attributes.getValue(ATTR_SUM);
      String avg = attributes.getValue(ATTR_AVG);
      String count = attributes.getValue(ATTR_COUNT);
      String fails = attributes.getValue(ATTR_FAILS);
      GraphTrend graphTrend = new GraphTrend(duration == null ? 0 : convert(Integer.class, duration, 0));

      if (sum != null) {
         graphTrend.setSum(sum);
      }

      if (avg != null) {
         graphTrend.setAvg(avg);
      }

      if (count != null) {
         graphTrend.setCount(count);
      }

      if (fails != null) {
         graphTrend.setFails(fails);
      }

      return graphTrend;
   }

   @Override
   public String buildIp(Attributes attributes) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Machine buildMachine(Attributes attributes) {
      String ip = attributes.getValue(ATTR_IP);
      Machine machine = new Machine(ip);

      return machine;
   }

   @Override
   public TransactionName buildName(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String totalCount = attributes.getValue(ATTR_TOTALCOUNT);
      String failCount = attributes.getValue(ATTR_FAILCOUNT);
      String failPercent = attributes.getValue(ATTR_FAILPERCENT);
      String min = attributes.getValue(ATTR_MIN);
      String max = attributes.getValue(ATTR_MAX);
      String avg = attributes.getValue(ATTR_AVG);
      String sum = attributes.getValue(ATTR_SUM);
      String sum2 = attributes.getValue(ATTR_SUM2);
      String std = attributes.getValue(ATTR_STD);
      String tps = attributes.getValue(ATTR_TPS);
      String line95Value = attributes.getValue(ATTR_LINE95VALUE);
      String line99Value = attributes.getValue(ATTR_LINE99VALUE);
      String line999Value = attributes.getValue(ATTR_LINE999VALUE);
      String line90Value = attributes.getValue(ATTR_LINE90VALUE);
      String line50Value = attributes.getValue(ATTR_LINE50VALUE);
      String line9999Value = attributes.getValue(ATTR_LINE9999VALUE);
      TransactionName name = new TransactionName(id);

      if (totalCount != null) {
         name.setTotalCount(convert(Long.class, totalCount, 0L));
      }

      if (failCount != null) {
         name.setFailCount(convert(Long.class, failCount, 0L));
      }

      if (failPercent != null) {
         name.setFailPercent(toNumber(failPercent, "0.00", 0).doubleValue());
      }

      if (min != null) {
         name.setMin(toNumber(min, "0.00", 0).doubleValue());
      }

      if (max != null) {
         name.setMax(toNumber(max, "0.00", 0).doubleValue());
      }

      if (avg != null) {
         name.setAvg(toNumber(avg, "0.0", 0).doubleValue());
      }

      if (sum != null) {
         name.setSum(toNumber(sum, "0.0", 0).doubleValue());
      }

      if (sum2 != null) {
         name.setSum2(toNumber(sum2, "0.0", 0).doubleValue());
      }

      if (std != null) {
         name.setStd(toNumber(std, "0.0", 0).doubleValue());
      }

      if (tps != null) {
         name.setTps(toNumber(tps, "0.00", 0).doubleValue());
      }

      if (line95Value != null) {
         name.setLine95Value(toNumber(line95Value, "0.00", 0).doubleValue());
      }

      if (line99Value != null) {
         name.setLine99Value(toNumber(line99Value, "0.00", 0).doubleValue());
      }

      if (line999Value != null) {
         name.setLine999Value(toNumber(line999Value, "0.00", 0).doubleValue());
      }

      if (line90Value != null) {
         name.setLine90Value(toNumber(line90Value, "0.00", 0).doubleValue());
      }

      if (line50Value != null) {
         name.setLine50Value(toNumber(line50Value, "0.00", 0).doubleValue());
      }

      if (line9999Value != null) {
         name.setLine9999Value(toNumber(line9999Value, "0.00", 0).doubleValue());
      }

      return name;
   }

   @Override
   public Range buildRange(Attributes attributes) {
      String value = attributes.getValue(ATTR_VALUE);
      String count = attributes.getValue(ATTR_COUNT);
      String sum = attributes.getValue(ATTR_SUM);
      String avg = attributes.getValue(ATTR_AVG);
      String fails = attributes.getValue(ATTR_FAILS);
      String min = attributes.getValue(ATTR_MIN);
      String max = attributes.getValue(ATTR_MAX);
      String line95Value = attributes.getValue(ATTR_LINE95VALUE);
      String line99Value = attributes.getValue(ATTR_LINE99VALUE);
      String line999Value = attributes.getValue(ATTR_LINE999VALUE);
      String line90Value = attributes.getValue(ATTR_LINE90VALUE);
      String line50Value = attributes.getValue(ATTR_LINE50VALUE);
      String line9999Value = attributes.getValue(ATTR_LINE9999VALUE);
      Range range = new Range(value == null ? 0 : convert(Integer.class, value, 0));

      if (count != null) {
         range.setCount(convert(Integer.class, count, 0));
      }

      if (sum != null) {
         range.setSum(convert(Double.class, sum, 0.0));
      }

      if (avg != null) {
         range.setAvg(toNumber(avg, "0.0", 0).doubleValue());
      }

      if (fails != null) {
         range.setFails(convert(Integer.class, fails, 0));
      }

      if (min != null) {
         range.setMin(toNumber(min, "0.00", 0).doubleValue());
      }

      if (max != null) {
         range.setMax(toNumber(max, "0.00", 0).doubleValue());
      }

      if (line95Value != null) {
         range.setLine95Value(toNumber(line95Value, "0.00", 0).doubleValue());
      }

      if (line99Value != null) {
         range.setLine99Value(toNumber(line99Value, "0.00", 0).doubleValue());
      }

      if (line999Value != null) {
         range.setLine999Value(toNumber(line999Value, "0.00", 0).doubleValue());
      }

      if (line90Value != null) {
         range.setLine90Value(toNumber(line90Value, "0.00", 0).doubleValue());
      }

      if (line50Value != null) {
         range.setLine50Value(toNumber(line50Value, "0.00", 0).doubleValue());
      }

      if (line9999Value != null) {
         range.setLine9999Value(toNumber(line9999Value, "0.00", 0).doubleValue());
      }

      return range;
   }

   @Override
   public Range2 buildRange2(Attributes attributes) {
      String value = attributes.getValue(ATTR_VALUE);
      String count = attributes.getValue(ATTR_COUNT);
      String sum = attributes.getValue(ATTR_SUM);
      String avg = attributes.getValue(ATTR_AVG);
      String fails = attributes.getValue(ATTR_FAILS);
      String min = attributes.getValue(ATTR_MIN);
      String max = attributes.getValue(ATTR_MAX);
      String line95Value = attributes.getValue(ATTR_LINE95VALUE);
      String line99Value = attributes.getValue(ATTR_LINE99VALUE);
      String line999Value = attributes.getValue(ATTR_LINE999VALUE);
      String line90Value = attributes.getValue(ATTR_LINE90VALUE);
      String line50Value = attributes.getValue(ATTR_LINE50VALUE);
      String line9999Value = attributes.getValue(ATTR_LINE9999VALUE);
      Range2 range2 = new Range2(value == null ? 0 : convert(Integer.class, value, 0));

      if (count != null) {
         range2.setCount(convert(Integer.class, count, 0));
      }

      if (sum != null) {
         range2.setSum(convert(Double.class, sum, 0.0));
      }

      if (avg != null) {
         range2.setAvg(toNumber(avg, "0.0", 0).doubleValue());
      }

      if (fails != null) {
         range2.setFails(convert(Integer.class, fails, 0));
      }

      if (min != null) {
         range2.setMin(toNumber(min, "0.00", 0).doubleValue());
      }

      if (max != null) {
         range2.setMax(toNumber(max, "0.00", 0).doubleValue());
      }

      if (line95Value != null) {
         range2.setLine95Value(toNumber(line95Value, "0.00", 0).doubleValue());
      }

      if (line99Value != null) {
         range2.setLine99Value(toNumber(line99Value, "0.00", 0).doubleValue());
      }

      if (line999Value != null) {
         range2.setLine999Value(toNumber(line999Value, "0.00", 0).doubleValue());
      }

      if (line90Value != null) {
         range2.setLine90Value(toNumber(line90Value, "0.00", 0).doubleValue());
      }

      if (line50Value != null) {
         range2.setLine50Value(toNumber(line50Value, "0.00", 0).doubleValue());
      }

      if (line9999Value != null) {
         range2.setLine9999Value(toNumber(line9999Value, "0.00", 0).doubleValue());
      }

      return range2;
   }

   @Override
   public StatusCode buildStatusCode(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String count = attributes.getValue(ATTR_COUNT);
      StatusCode statusCode = new StatusCode(id);

      if (count != null) {
         statusCode.setCount(convert(Long.class, count, 0L));
      }

      return statusCode;
   }

   @Override
   public TransactionReport buildTransactionReport(Attributes attributes) {
      String domain = attributes.getValue(ATTR_DOMAIN);
      String startTime = attributes.getValue(ATTR_STARTTIME);
      String endTime = attributes.getValue(ATTR_ENDTIME);
      TransactionReport transactionReport = new TransactionReport(domain);

      if (startTime != null) {
         transactionReport.setStartTime(toDate(startTime, "yyyy-MM-dd HH:mm:ss", null));
      }

      if (endTime != null) {
         transactionReport.setEndTime(toDate(endTime, "yyyy-MM-dd HH:mm:ss", null));
      }

      return transactionReport;
   }

   @Override
   public TransactionType buildType(Attributes attributes) {
      String id = attributes.getValue(ATTR_ID);
      String totalCount = attributes.getValue(ATTR_TOTALCOUNT);
      String failCount = attributes.getValue(ATTR_FAILCOUNT);
      String failPercent = attributes.getValue(ATTR_FAILPERCENT);
      String min = attributes.getValue(ATTR_MIN);
      String max = attributes.getValue(ATTR_MAX);
      String avg = attributes.getValue(ATTR_AVG);
      String sum = attributes.getValue(ATTR_SUM);
      String sum2 = attributes.getValue(ATTR_SUM2);
      String std = attributes.getValue(ATTR_STD);
      String tps = attributes.getValue(ATTR_TPS);
      String line95Value = attributes.getValue(ATTR_LINE95VALUE);
      String line99Value = attributes.getValue(ATTR_LINE99VALUE);
      String line999Value = attributes.getValue(ATTR_LINE999VALUE);
      String line90Value = attributes.getValue(ATTR_LINE90VALUE);
      String line50Value = attributes.getValue(ATTR_LINE50VALUE);
      String line9999Value = attributes.getValue(ATTR_LINE9999VALUE);
      TransactionType type = new TransactionType(id);

      if (totalCount != null) {
         type.setTotalCount(convert(Long.class, totalCount, 0L));
      }

      if (failCount != null) {
         type.setFailCount(convert(Long.class, failCount, 0L));
      }

      if (failPercent != null) {
         type.setFailPercent(toNumber(failPercent, "0.00", 0).doubleValue());
      }

      if (min != null) {
         type.setMin(toNumber(min, "0.00", 0).doubleValue());
      }

      if (max != null) {
         type.setMax(toNumber(max, "0.00", 0).doubleValue());
      }

      if (avg != null) {
         type.setAvg(toNumber(avg, "0.0", 0).doubleValue());
      }

      if (sum != null) {
         type.setSum(toNumber(sum, "0.0", 0).doubleValue());
      }

      if (sum2 != null) {
         type.setSum2(toNumber(sum2, "0.0", 0).doubleValue());
      }

      if (std != null) {
         type.setStd(toNumber(std, "0.0", 0).doubleValue());
      }

      if (tps != null) {
         type.setTps(toNumber(tps, "0.00", 0).doubleValue());
      }

      if (line95Value != null) {
         type.setLine95Value(toNumber(line95Value, "0.00", 0).doubleValue());
      }

      if (line99Value != null) {
         type.setLine99Value(toNumber(line99Value, "0.00", 0).doubleValue());
      }

      if (line999Value != null) {
         type.setLine999Value(toNumber(line999Value, "0.00", 0).doubleValue());
      }

      if (line90Value != null) {
         type.setLine90Value(toNumber(line90Value, "0.00", 0).doubleValue());
      }

      if (line50Value != null) {
         type.setLine50Value(toNumber(line50Value, "0.00", 0).doubleValue());
      }

      if (line9999Value != null) {
         type.setLine9999Value(toNumber(line9999Value, "0.00", 0).doubleValue());
      }

      Map<String, String> dynamicAttributes = type.getDynamicAttributes();
      int _length = attributes == null ? 0 : attributes.getLength();

      for (int i = 0; i < _length; i++) {
         String _name = attributes.getQName(i);
         String _value = attributes.getValue(i);

         dynamicAttributes.put(_name, _value);
      }

      dynamicAttributes.remove(ATTR_ID);
      dynamicAttributes.remove(ATTR_TOTALCOUNT);
      dynamicAttributes.remove(ATTR_FAILCOUNT);
      dynamicAttributes.remove(ATTR_FAILPERCENT);
      dynamicAttributes.remove(ATTR_MIN);
      dynamicAttributes.remove(ATTR_MAX);
      dynamicAttributes.remove(ATTR_AVG);
      dynamicAttributes.remove(ATTR_SUM);
      dynamicAttributes.remove(ATTR_SUM2);
      dynamicAttributes.remove(ATTR_STD);
      dynamicAttributes.remove(ATTR_TPS);
      dynamicAttributes.remove(ATTR_LINE95VALUE);
      dynamicAttributes.remove(ATTR_LINE99VALUE);
      dynamicAttributes.remove(ATTR_LINE999VALUE);
      dynamicAttributes.remove(ATTR_LINE90VALUE);
      dynamicAttributes.remove(ATTR_LINE50VALUE);
      dynamicAttributes.remove(ATTR_LINE9999VALUE);

      return type;
   }

   @SuppressWarnings("unchecked")
   protected <T> T convert(Class<T> type, String value, T defaultValue) {
      if (value == null || value.length() == 0) {
         return defaultValue;
      }

      if (type == Boolean.class || type == Boolean.TYPE) {
         return (T) Boolean.valueOf(value);
      } else if (type == Integer.class || type == Integer.TYPE) {
         return (T) Integer.valueOf(value);
      } else if (type == Long.class || type == Long.TYPE) {
         return (T) Long.valueOf(value);
      } else if (type == Short.class || type == Short.TYPE) {
         return (T) Short.valueOf(value);
      } else if (type == Float.class || type == Float.TYPE) {
         return (T) Float.valueOf(value);
      } else if (type == Double.class || type == Double.TYPE) {
         return (T) Double.valueOf(value);
      } else if (type == Byte.class || type == Byte.TYPE) {
         return (T) Byte.valueOf(value);
      } else if (type == Character.class || type == Character.TYPE) {
         return (T) (Character) value.charAt(0);
      } else {
         return (T) value;
      }
   }

   protected java.util.Date toDate(String str, String format, java.util.Date defaultValue) {
      if (str == null || str.length() == 0) {
         return defaultValue;
      }

      try {
         return new java.text.SimpleDateFormat(format).parse(str);
      } catch (java.text.ParseException e) {
         throw new RuntimeException(String.format("Unable to parse date(%s) in format(%s)!", str, format), e);
      }
   }

   protected Number toNumber(String str, String format, Number defaultValue) {
      if (str == null || str.length() == 0) {
         return defaultValue;
      }

      try {
         return new java.text.DecimalFormat(format).parse(str);
      } catch (java.text.ParseException e) {
         throw new RuntimeException(String.format("Unable to parse number(%s) in format(%s)!", str, format), e);
      }
   }
}
