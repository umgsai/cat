package com.dianping.cat.consumer.transaction.model;

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

public interface IVisitor {

   public void visitAllDuration(AllDuration allDuration);

   public void visitDuration(Duration duration);

   public void visitGraph(Graph graph);

   public void visitGraph2(Graph2 graph2);

   public void visitGraphTrend(GraphTrend graphTrend);

   public void visitMachine(Machine machine);

   public void visitName(TransactionName name);

   public void visitRange(Range range);

   public void visitRange2(Range2 range2);

   public void visitStatusCode(StatusCode statusCode);

   public void visitTransactionReport(TransactionReport transactionReport);

   public void visitType(TransactionType type);
}
