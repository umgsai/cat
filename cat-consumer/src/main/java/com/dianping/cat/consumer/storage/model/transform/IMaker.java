package com.dianping.cat.consumer.storage.model.transform;

import com.dianping.cat.consumer.storage.model.entity.Domain;
import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.entity.Segment;
import com.dianping.cat.consumer.storage.model.entity.Sql;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;

public interface IMaker<T> {

   Domain buildDomain(T node);

   String buildId(T node);

   String buildIp(T node);

   Machine buildMachine(T node);

   String buildOp(T node);

   Operation buildOperation(T node);

   Segment buildSegment(T node);

   Sql buildSql(T node);

   StorageReport buildStorageReport(T node);
}
