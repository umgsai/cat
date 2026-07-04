package com.dianping.cat.consumer.transaction.model.transform;

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

public interface IMaker<T> {

   AllDuration buildAllDuration(T node);

   String buildDomain(T node);

   Duration buildDuration(T node);

   Graph buildGraph(T node);

   Graph2 buildGraph2(T node);

   GraphTrend buildGraphTrend(T node);

   String buildIp(T node);

   Machine buildMachine(T node);

   TransactionName buildName(T node);

   Range buildRange(T node);

   Range2 buildRange2(T node);

   StatusCode buildStatusCode(T node);

   TransactionReport buildTransactionReport(T node);

   TransactionType buildType(T node);
}
