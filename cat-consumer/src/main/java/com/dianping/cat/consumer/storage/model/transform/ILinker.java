package com.dianping.cat.consumer.storage.model.transform;

import com.dianping.cat.consumer.storage.model.entity.Domain;
import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.entity.Segment;
import com.dianping.cat.consumer.storage.model.entity.Sql;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;

public interface ILinker {

   boolean onDomain(Machine parent, Domain domain);

   boolean onMachine(StorageReport parent, Machine machine);

   boolean onOperation(Domain parent, Operation operation);

   boolean onSegment(Operation parent, Segment segment);

   boolean onSql(Domain parent, Sql sql);
}
