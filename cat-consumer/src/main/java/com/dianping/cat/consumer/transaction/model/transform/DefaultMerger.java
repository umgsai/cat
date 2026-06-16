package com.dianping.cat.consumer.transaction.model.transform;

import java.util.Stack;

import com.dianping.cat.consumer.transaction.model.IEntity;
import com.dianping.cat.consumer.transaction.model.IVisitor;
import com.dianping.cat.consumer.transaction.model.entity.AllDuration;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Graph;
import com.dianping.cat.consumer.transaction.model.entity.Graph2;
import com.dianping.cat.consumer.transaction.model.entity.GraphTrend;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.Range2;
import com.dianping.cat.consumer.transaction.model.entity.StatusCode;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;

public class DefaultMerger implements IVisitor {

   private Stack<Object> m_objs = new Stack<Object>();

   private TransactionReport m_transactionReport;

   public DefaultMerger() {
   }

   public DefaultMerger(TransactionReport transactionReport) {
      m_transactionReport = transactionReport;
      m_objs.push(transactionReport);
   }

   public TransactionReport getTransactionReport() {
      return m_transactionReport;
   }

   protected Stack<Object> getObjects() {
      return m_objs;
   }

   public <T> void merge(IEntity<T> to, IEntity<T> from) {
      m_objs.push(to);
      from.accept(this);
      m_objs.pop();
   }

   protected void mergeAllDuration(AllDuration to, AllDuration from) {
      to.mergeAttributes(from);
   }

   protected void mergeDuration(Duration to, Duration from) {
      to.mergeAttributes(from);
   }

   protected void mergeGraph(Graph to, Graph from) {
      to.mergeAttributes(from);
   }

   protected void mergeGraph2(Graph2 to, Graph2 from) {
      to.mergeAttributes(from);
   }

   protected void mergeGraphTrend(GraphTrend to, GraphTrend from) {
      to.mergeAttributes(from);
   }

   protected void mergeMachine(Machine to, Machine from) {
      to.mergeAttributes(from);
   }

   protected void mergeName(TransactionName to, TransactionName from) {
      to.mergeAttributes(from);
      to.setSuccessMessageUrl(from.getSuccessMessageUrl());
      to.setFailMessageUrl(from.getFailMessageUrl());
      to.setLongestMessageUrl(from.getLongestMessageUrl());
   }

   protected void mergeRange(Range to, Range from) {
      to.mergeAttributes(from);
   }

   protected void mergeRange2(Range2 to, Range2 from) {
      to.mergeAttributes(from);
   }

   protected void mergeStatusCode(StatusCode to, StatusCode from) {
      to.mergeAttributes(from);
   }

   protected void mergeTransactionReport(TransactionReport to, TransactionReport from) {
      to.mergeAttributes(from);
      to.getDomainNames().addAll(from.getDomainNames());
      to.getIps().addAll(from.getIps());
   }

   protected void mergeType(TransactionType to, TransactionType from) {
      to.mergeAttributes(from);
      to.setSuccessMessageUrl(from.getSuccessMessageUrl());
      to.setFailMessageUrl(from.getFailMessageUrl());
      to.setLongestMessageUrl(from.getLongestMessageUrl());
   }

   @Override
   public void visitAllDuration(AllDuration from) {
      AllDuration to = (AllDuration) m_objs.peek();

      mergeAllDuration(to, from);
      visitAllDurationChildren(to, from);
   }

   protected void visitAllDurationChildren(AllDuration to, AllDuration from) {
   }

   @Override
   public void visitDuration(Duration from) {
      Duration to = (Duration) m_objs.peek();

      mergeDuration(to, from);
      visitDurationChildren(to, from);
   }

   protected void visitDurationChildren(Duration to, Duration from) {
   }

   @Override
   public void visitGraph(Graph from) {
      Graph to = (Graph) m_objs.peek();

      mergeGraph(to, from);
      visitGraphChildren(to, from);
   }

   protected void visitGraphChildren(Graph to, Graph from) {
   }

   @Override
   public void visitGraph2(Graph2 from) {
      Graph2 to = (Graph2) m_objs.peek();

      mergeGraph2(to, from);
      visitGraph2Children(to, from);
   }

   protected void visitGraph2Children(Graph2 to, Graph2 from) {
   }

   @Override
   public void visitGraphTrend(GraphTrend from) {
      GraphTrend to = (GraphTrend) m_objs.peek();

      mergeGraphTrend(to, from);
      visitGraphTrendChildren(to, from);
   }

   protected void visitGraphTrendChildren(GraphTrend to, GraphTrend from) {
   }

   @Override
   public void visitMachine(Machine from) {
      Machine to = (Machine) m_objs.peek();

      mergeMachine(to, from);
      visitMachineChildren(to, from);
   }

   protected void visitMachineChildren(Machine to, Machine from) {
      for (TransactionType source : from.getTypes().values()) {
         TransactionType target = to.findType(source.getId());

         if (target == null) {
            target = new TransactionType(source.getId());
            to.addType(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitName(TransactionName from) {
      TransactionName to = (TransactionName) m_objs.peek();

      mergeName(to, from);
      visitNameChildren(to, from);
   }

   protected void visitNameChildren(TransactionName to, TransactionName from) {
      for (Range source : from.getRanges().values()) {
         Range target = to.findRange(source.getValue());

         if (target == null) {
            target = new Range(source.getValue());
            to.addRange(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }

      for (Duration source : from.getDurations().values()) {
         Duration target = to.findDuration(source.getValue());

         if (target == null) {
            target = new Duration(source.getValue());
            to.addDuration(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }

      for (Graph source : from.getGraphs().values()) {
         Graph target = to.findGraph(source.getDuration());

         if (target == null) {
            target = new Graph(source.getDuration());
            to.addGraph(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }

      for (AllDuration source : from.getAllDurations().values()) {
         AllDuration target = to.findAllDuration(source.getValue());

         if (target == null) {
            target = new AllDuration(source.getValue());
            to.addAllDuration(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }

      if (from.getGraphTrend() != null) {
         GraphTrend target = to.getGraphTrend();

         if (target == null) {
            GraphTrend source = from.getGraphTrend();

            target = new GraphTrend(source.getDuration());
            to.setGraphTrend(target);
         }

         m_objs.push(target);
         from.getGraphTrend().accept(this);
         m_objs.pop();
      }

      for (StatusCode source : from.getStatusCodes().values()) {
         StatusCode target = to.findStatusCode(source.getId());

         if (target == null) {
            target = new StatusCode(source.getId());
            to.addStatusCode(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitRange(Range from) {
      Range to = (Range) m_objs.peek();

      mergeRange(to, from);
      visitRangeChildren(to, from);
   }

   protected void visitRangeChildren(Range to, Range from) {
      for (AllDuration source : from.getAllDurations().values()) {
         AllDuration target = to.findAllDuration(source.getValue());

         if (target == null) {
            target = new AllDuration(source.getValue());
            to.addAllDuration(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitRange2(Range2 from) {
      Range2 to = (Range2) m_objs.peek();

      mergeRange2(to, from);
      visitRange2Children(to, from);
   }

   protected void visitRange2Children(Range2 to, Range2 from) {
      for (AllDuration source : from.getAllDurations().values()) {
         AllDuration target = to.findAllDuration(source.getValue());

         if (target == null) {
            target = new AllDuration(source.getValue());
            to.addAllDuration(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitStatusCode(StatusCode from) {
      StatusCode to = (StatusCode) m_objs.peek();

      mergeStatusCode(to, from);
      visitStatusCodeChildren(to, from);
   }

   protected void visitStatusCodeChildren(StatusCode to, StatusCode from) {
   }

   @Override
   public void visitTransactionReport(TransactionReport from) {
      TransactionReport to = (TransactionReport) m_objs.peek();

      mergeTransactionReport(to, from);
      visitTransactionReportChildren(to, from);
   }

   protected void visitTransactionReportChildren(TransactionReport to, TransactionReport from) {
      for (Machine source : from.getMachines().values()) {
         Machine target = to.findMachine(source.getIp());

         if (target == null) {
            target = new Machine(source.getIp());
            to.addMachine(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }

   @Override
   public void visitType(TransactionType from) {
      TransactionType to = (TransactionType) m_objs.peek();

      mergeType(to, from);
      visitTypeChildren(to, from);
   }

   protected void visitTypeChildren(TransactionType to, TransactionType from) {
      for (TransactionName source : from.getNames().values()) {
         TransactionName target = to.findName(source.getId());

         if (target == null) {
            target = new TransactionName(source.getId());
            to.addName(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }

      for (Graph2 source : from.getGraph2s().values()) {
         Graph2 target = to.findGraph2(source.getDuration());

         if (target == null) {
            target = new Graph2(source.getDuration());
            to.addGraph2(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }

      for (AllDuration source : from.getAllDurations().values()) {
         AllDuration target = to.findAllDuration(source.getValue());

         if (target == null) {
            target = new AllDuration(source.getValue());
            to.addAllDuration(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }

      if (from.getGraphTrend() != null) {
         GraphTrend target = to.getGraphTrend();

         if (target == null) {
            GraphTrend source = from.getGraphTrend();

            target = new GraphTrend(source.getDuration());
            to.setGraphTrend(target);
         }

         m_objs.push(target);
         from.getGraphTrend().accept(this);
         m_objs.pop();
      }

      for (Range2 source : from.getRange2s().values()) {
         Range2 target = to.findRange2(source.getValue());

         if (target == null) {
            target = new Range2(source.getValue());
            to.addRange2(target);
         }

         m_objs.push(target);
         source.accept(this);
         m_objs.pop();
      }
   }
}
