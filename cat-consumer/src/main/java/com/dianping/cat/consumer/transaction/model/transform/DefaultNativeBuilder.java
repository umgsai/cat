package com.dianping.cat.consumer.transaction.model.transform;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Map;

import com.dianping.cat.consumer.transaction.model.IVisitor;
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

public class DefaultNativeBuilder implements IVisitor {

   private IVisitor m_visitor;

   private DataOutputStream m_out;

   public DefaultNativeBuilder(OutputStream out) {
      this(out, null);
   }

   public DefaultNativeBuilder(OutputStream out, IVisitor visitor) {
      m_out = new DataOutputStream(out);
      m_visitor = (visitor == null ? this : visitor);
   }

   public static byte[] build(TransactionReport transactionReport) {
      ByteArrayOutputStream out = new ByteArrayOutputStream(8192);

      build(transactionReport, out);
      return out.toByteArray();
   }

   public static void build(TransactionReport transactionReport, OutputStream out) {
      transactionReport.accept(new DefaultNativeBuilder(out));
   }

   @Override
   public void visitAllDuration(AllDuration allDuration) {
      writeTag(1, 0);
      writeInt(allDuration.getValue());

      writeTag(2, 0);
      writeInt(allDuration.getCount());

      writeTag(63, 3);
   }

   @Override
   public void visitDuration(Duration duration) {
      writeTag(1, 0);
      writeInt(duration.getValue());

      writeTag(2, 0);
      writeInt(duration.getCount());

      writeTag(63, 3);
   }

   @Override
   public void visitGraph(Graph graph) {
      writeTag(1, 0);
      writeInt(graph.getDuration());

      writeTag(2, 0);
      writeString(graph.getSum());

      writeTag(3, 0);
      writeString(graph.getAvg());

      writeTag(4, 0);
      writeString(graph.getCount());

      writeTag(5, 0);
      writeString(graph.getFails());

      writeTag(63, 3);
   }

   @Override
   public void visitGraph2(Graph2 graph2) {
      writeTag(1, 0);
      writeInt(graph2.getDuration());

      writeTag(2, 0);
      writeString(graph2.getSum());

      writeTag(3, 0);
      writeString(graph2.getAvg());

      writeTag(4, 0);
      writeString(graph2.getCount());

      writeTag(5, 0);
      writeString(graph2.getFails());

      writeTag(63, 3);
   }

   @Override
   public void visitGraphTrend(GraphTrend graphTrend) {
      writeTag(1, 0);
      writeInt(graphTrend.getDuration());

      writeTag(2, 0);
      writeString(graphTrend.getSum());

      writeTag(3, 0);
      writeString(graphTrend.getAvg());

      writeTag(4, 0);
      writeString(graphTrend.getCount());

      writeTag(5, 0);
      writeString(graphTrend.getFails());

      writeTag(63, 3);
   }

   @Override
   public void visitMachine(Machine machine) {
      if (machine.getIp() != null) {
         writeTag(1, 1);
         writeString(machine.getIp());
      }

      if (!machine.getTypes().isEmpty()) {
         writeTag(33, 2);
         writeInt(machine.getTypes().size());

         for (TransactionType type : machine.getTypes().values()) {
            type.accept(m_visitor);
         }
      }

      writeTag(63, 3);
   }

   @Override
   public void visitName(TransactionName name) {
      if (name.getId() != null) {
         writeTag(1, 1);
         writeString(name.getId());
      }

      writeTag(2, 0);
      writeLong(name.getTotalCount());

      writeTag(3, 0);
      writeLong(name.getFailCount());

      writeTag(4, 0);
      writeDouble(name.getFailPercent());

      writeTag(5, 0);
      writeDouble(name.getMin());

      writeTag(6, 0);
      writeDouble(name.getMax());

      writeTag(7, 0);
      writeDouble(name.getAvg());

      writeTag(8, 0);
      writeDouble(name.getSum());

      writeTag(9, 0);
      writeDouble(name.getSum2());

      writeTag(10, 0);
      writeDouble(name.getStd());

      if (name.getSuccessMessageUrl() != null) {
         writeTag(11, 1);
         writeString(name.getSuccessMessageUrl());
      }

      if (name.getFailMessageUrl() != null) {
         writeTag(12, 1);
         writeString(name.getFailMessageUrl());
      }

      writeTag(13, 0);
      writeDouble(name.getTps());

      writeTag(14, 0);
      writeDouble(name.getLine95Value());

      writeTag(15, 0);
      writeDouble(name.getLine99Value());

      writeTag(16, 0);
      writeDouble(name.getLine999Value());

      writeTag(17, 0);
      writeDouble(name.getLine90Value());

      writeTag(18, 0);
      writeDouble(name.getLine50Value());

      writeTag(19, 0);
      writeDouble(name.getLine9999Value());

      if (name.getLongestMessageUrl() != null) {
         writeTag(20, 1);
         writeString(name.getLongestMessageUrl());
      }

      if (!name.getRanges().isEmpty()) {
         writeTag(33, 2);
         writeInt(name.getRanges().size());

         for (Range range : name.getRanges().values()) {
            range.accept(m_visitor);
         }
      }

      if (!name.getDurations().isEmpty()) {
         writeTag(34, 2);
         writeInt(name.getDurations().size());

         for (Duration duration : name.getDurations().values()) {
            duration.accept(m_visitor);
         }
      }

      if (!name.getGraphs().isEmpty()) {
         writeTag(35, 2);
         writeInt(name.getGraphs().size());

         for (Graph graph : name.getGraphs().values()) {
            graph.accept(m_visitor);
         }
      }

      if (name.getGraphTrend() != null) {
         writeTag(36, 1);
         name.getGraphTrend().accept(m_visitor);
      }

      if (!name.getStatusCodes().isEmpty()) {
         writeTag(37, 2);
         writeInt(name.getStatusCodes().size());

         for (StatusCode statusCode : name.getStatusCodes().values()) {
            statusCode.accept(m_visitor);
         }
      }

      writeTag(63, 3);
   }

   @Override
   public void visitRange(Range range) {
      writeTag(1, 0);
      writeInt(range.getValue());

      writeTag(2, 0);
      writeInt(range.getCount());

      writeTag(3, 0);
      writeDouble(range.getSum());

      writeTag(4, 0);
      writeDouble(range.getAvg());

      writeTag(5, 0);
      writeInt(range.getFails());

      writeTag(6, 0);
      writeDouble(range.getMin());

      writeTag(7, 0);
      writeDouble(range.getMax());

      writeTag(8, 0);
      writeDouble(range.getLine95Value());

      writeTag(9, 0);
      writeDouble(range.getLine99Value());

      writeTag(10, 0);
      writeDouble(range.getLine999Value());

      writeTag(11, 0);
      writeDouble(range.getLine90Value());

      writeTag(12, 0);
      writeDouble(range.getLine50Value());

      writeTag(13, 0);
      writeDouble(range.getLine9999Value());

      writeTag(63, 3);
   }

   @Override
   public void visitRange2(Range2 range2) {
      writeTag(1, 0);
      writeInt(range2.getValue());

      writeTag(2, 0);
      writeInt(range2.getCount());

      writeTag(3, 0);
      writeDouble(range2.getSum());

      writeTag(4, 0);
      writeDouble(range2.getAvg());

      writeTag(5, 0);
      writeInt(range2.getFails());

      writeTag(6, 0);
      writeDouble(range2.getMin());

      writeTag(7, 0);
      writeDouble(range2.getMax());

      writeTag(8, 0);
      writeDouble(range2.getLine95Value());

      writeTag(9, 0);
      writeDouble(range2.getLine99Value());

      writeTag(10, 0);
      writeDouble(range2.getLine999Value());

      writeTag(11, 0);
      writeDouble(range2.getLine90Value());

      writeTag(12, 0);
      writeDouble(range2.getLine50Value());

      writeTag(13, 0);
      writeDouble(range2.getLine9999Value());

      writeTag(63, 3);
   }

   @Override
   public void visitStatusCode(StatusCode statusCode) {
      if (statusCode.getId() != null) {
         writeTag(1, 1);
         writeString(statusCode.getId());
      }

      writeTag(2, 0);
      writeLong(statusCode.getCount());

      writeTag(63, 3);
   }

   @Override
   public void visitTransactionReport(TransactionReport transactionReport) {
      writeTag(63, 0);

      if (transactionReport.getDomain() != null) {
         writeTag(1, 1);
         writeString(transactionReport.getDomain());
      }

      if (transactionReport.getStartTime() != null) {
         writeTag(2, 1);
         writeDate(transactionReport.getStartTime());
      }

      if (transactionReport.getEndTime() != null) {
         writeTag(3, 1);
         writeDate(transactionReport.getEndTime());
      }

      if (transactionReport.getDomainNames() != null) {
         writeTag(4, 2);
         writeInt(transactionReport.getDomainNames().size());

         for (String domain : transactionReport.getDomainNames()) {
            writeString(domain);
         }
      }

      if (transactionReport.getIps() != null) {
         writeTag(5, 2);
         writeInt(transactionReport.getIps().size());

         for (String ip : transactionReport.getIps()) {
            writeString(ip);
         }
      }

      if (!transactionReport.getMachines().isEmpty()) {
         writeTag(33, 2);
         writeInt(transactionReport.getMachines().size());

         for (Machine machine : transactionReport.getMachines().values()) {
            machine.accept(m_visitor);
         }
      }

      writeTag(63, 3);
   }

   @Override
   public void visitType(TransactionType type) {
      if (type.getId() != null) {
         writeTag(1, 1);
         writeString(type.getId());
      }

      writeTag(2, 0);
      writeLong(type.getTotalCount());

      writeTag(3, 0);
      writeLong(type.getFailCount());

      writeTag(4, 0);
      writeDouble(type.getFailPercent());

      writeTag(5, 0);
      writeDouble(type.getMin());

      writeTag(6, 0);
      writeDouble(type.getMax());

      writeTag(7, 0);
      writeDouble(type.getAvg());

      writeTag(8, 0);
      writeDouble(type.getSum());

      writeTag(9, 0);
      writeDouble(type.getSum2());

      writeTag(10, 0);
      writeDouble(type.getStd());

      if (type.getSuccessMessageUrl() != null) {
         writeTag(11, 1);
         writeString(type.getSuccessMessageUrl());
      }

      if (type.getFailMessageUrl() != null) {
         writeTag(12, 1);
         writeString(type.getFailMessageUrl());
      }

      writeTag(13, 0);
      writeDouble(type.getTps());

      writeTag(14, 0);
      writeDouble(type.getLine95Value());

      writeTag(15, 0);
      writeDouble(type.getLine99Value());

      writeTag(16, 0);
      writeDouble(type.getLine999Value());

      writeTag(17, 0);
      writeDouble(type.getLine90Value());

      writeTag(18, 0);
      writeDouble(type.getLine50Value());

      writeTag(19, 0);
      writeDouble(type.getLine9999Value());

      if (type.getLongestMessageUrl() != null) {
         writeTag(20, 1);
         writeString(type.getLongestMessageUrl());
      }

      if (!type.getNames().isEmpty()) {
         writeTag(33, 2);
         writeInt(type.getNames().size());

         for (TransactionName name : type.getNames().values()) {
            name.accept(m_visitor);
         }
      }

      if (!type.getGraph2s().isEmpty()) {
         writeTag(34, 2);
         writeInt(type.getGraph2s().size());

         for (Graph2 graph2 : type.getGraph2s().values()) {
            graph2.accept(m_visitor);
         }
      }

      if (type.getGraphTrend() != null) {
         writeTag(35, 1);
         type.getGraphTrend().accept(m_visitor);
      }

      if (!type.getRange2s().isEmpty()) {
         writeTag(36, 2);
         writeInt(type.getRange2s().size());

         for (Range2 range2 : type.getRange2s().values()) {
            range2.accept(m_visitor);
         }
      }

      if (!type.getDynamicAttributes().isEmpty()) {
         writeTag(63, 2);
         writeInt(type.getDynamicAttributes().size());

         for (Map.Entry<String, String> dynamicAttribute : type.getDynamicAttributes().entrySet()) {
            writeString(dynamicAttribute.getKey());
            writeString(dynamicAttribute.getValue());
         }
      }

      writeTag(63, 3);
   }

   private void writeDate(java.util.Date value) {
      try {
         writeVarint(value.getTime());
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void writeDouble(double value) {
      try {
         writeVarint(Double.doubleToLongBits(value));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void writeInt(int value) {
      try {
         writeVarint(value & 0xFFFFFFFFL);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void writeLong(long value) {
      try {
         writeVarint(value);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void writeString(String value) {
      try {
         m_out.writeUTF(value);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void writeTag(int field, int type) {
      try {
         m_out.writeByte((field << 2) + type);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   protected void writeVarint(long value) throws IOException {
      while (true) {
         if ((value & ~0x7FL) == 0) {
            m_out.writeByte((byte) value);
            return;
         } else {
            m_out.writeByte(((byte) value & 0x7F) | 0x80);
            value >>>= 7;
         }
      }
   }
}
