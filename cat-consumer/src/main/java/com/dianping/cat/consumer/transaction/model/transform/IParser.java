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

public interface IParser<T> {
   TransactionReport parse(IMaker<T> maker, ILinker linker, T node);

   void parseForAllDuration(IMaker<T> maker, ILinker linker, AllDuration parent, T node);

   void parseForDuration(IMaker<T> maker, ILinker linker, Duration parent, T node);

   void parseForGraph(IMaker<T> maker, ILinker linker, Graph parent, T node);

   void parseForGraph2(IMaker<T> maker, ILinker linker, Graph2 parent, T node);

   void parseForGraphTrend(IMaker<T> maker, ILinker linker, GraphTrend parent, T node);

   void parseForMachine(IMaker<T> maker, ILinker linker, Machine parent, T node);

   void parseForTransactionName(IMaker<T> maker, ILinker linker, TransactionName parent, T node);

   void parseForRange(IMaker<T> maker, ILinker linker, Range parent, T node);

   void parseForRange2(IMaker<T> maker, ILinker linker, Range2 parent, T node);

   void parseForStatusCode(IMaker<T> maker, ILinker linker, StatusCode parent, T node);

   void parseForTransactionType(IMaker<T> maker, ILinker linker, TransactionType parent, T node);
}
