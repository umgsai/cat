package com.dianping.cat.consumer.transaction.model.transform;

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

public abstract class BaseVisitor implements IVisitor {
   @Override
   public void visitAllDuration(AllDuration allDuration) {
   }

   @Override
   public void visitDuration(Duration duration) {
   }

   @Override
   public void visitGraph(Graph graph) {
   }

   @Override
   public void visitGraph2(Graph2 graph2) {
   }

   @Override
   public void visitGraphTrend(GraphTrend graphTrend) {
   }

   @Override
   public void visitMachine(Machine machine) {
      for (TransactionType type : machine.getTypes().values()) {
         visitType(type);
      }
   }

   @Override
   public void visitName(TransactionName name) {
      for (Range range : name.getRanges().values()) {
         visitRange(range);
      }

      for (Duration duration : name.getDurations().values()) {
         visitDuration(duration);
      }

      for (Graph graph : name.getGraphs().values()) {
         visitGraph(graph);
      }

      for (AllDuration allDuration : name.getAllDurations().values()) {
         visitAllDuration(allDuration);
      }

      if (name.getGraphTrend() != null) {
         visitGraphTrend(name.getGraphTrend());
      }

      for (StatusCode statusCode : name.getStatusCodes().values()) {
         visitStatusCode(statusCode);
      }
   }

   @Override
   public void visitRange(Range range) {
      for (AllDuration allDuration : range.getAllDurations().values()) {
         visitAllDuration(allDuration);
      }
   }

   @Override
   public void visitRange2(Range2 range2) {
      for (AllDuration allDuration : range2.getAllDurations().values()) {
         visitAllDuration(allDuration);
      }
   }

   @Override
   public void visitStatusCode(StatusCode statusCode) {
   }

   @Override
   public void visitTransactionReport(TransactionReport transactionReport) {
      for (Machine machine : transactionReport.getMachines().values()) {
         visitMachine(machine);
      }
   }

   @Override
   public void visitType(TransactionType type) {
      for (TransactionName name : type.getNames().values()) {
         visitName(name);
      }

      for (Graph2 graph2 : type.getGraph2s().values()) {
         visitGraph2(graph2);
      }

      for (AllDuration allDuration : type.getAllDurations().values()) {
         visitAllDuration(allDuration);
      }

      if (type.getGraphTrend() != null) {
         visitGraphTrend(type.getGraphTrend());
      }

      for (Range2 range2 : type.getRange2s().values()) {
         visitRange2(range2);
      }
   }
}
