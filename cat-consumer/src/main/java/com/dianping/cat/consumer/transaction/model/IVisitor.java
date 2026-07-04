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

   void visitAllDuration(AllDuration allDuration);

   void visitDuration(Duration duration);

   void visitGraph(Graph graph);

   void visitGraph2(Graph2 graph2);

   void visitGraphTrend(GraphTrend graphTrend);

   void visitMachine(Machine machine);

   void visitName(TransactionName name);

   void visitRange(Range range);

   void visitRange2(Range2 range2);

   void visitStatusCode(StatusCode statusCode);

   void visitTransactionReport(TransactionReport transactionReport);

   void visitType(TransactionType type);
}
