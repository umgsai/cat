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

public interface ILinker {

   boolean onAllDuration(TransactionType parent, AllDuration allDuration);

   boolean onAllDuration(TransactionName parent, AllDuration allDuration);

   boolean onAllDuration(Range parent, AllDuration allDuration);

   boolean onAllDuration(Range2 parent, AllDuration allDuration);

   boolean onDuration(TransactionName parent, Duration duration);

   boolean onGraph(TransactionName parent, Graph graph);

   boolean onGraph2(TransactionType parent, Graph2 graph2);

   boolean onGraphTrend(TransactionType parent, GraphTrend graphTrend);

   boolean onGraphTrend(TransactionName parent, GraphTrend graphTrend);

   boolean onMachine(TransactionReport parent, Machine machine);

   boolean onName(TransactionType parent, TransactionName name);

   boolean onRange(TransactionName parent, Range range);

   boolean onRange2(TransactionType parent, Range2 range2);

   boolean onStatusCode(TransactionName parent, StatusCode statusCode);

   boolean onType(Machine parent, TransactionType type);
}
