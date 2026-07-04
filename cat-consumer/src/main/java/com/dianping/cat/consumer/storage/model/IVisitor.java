package com.dianping.cat.consumer.storage.model;

import com.dianping.cat.consumer.storage.model.entity.Domain;
import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.entity.Segment;
import com.dianping.cat.consumer.storage.model.entity.Sql;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;

public interface IVisitor {

   void visitDomain(Domain domain);

   void visitMachine(Machine machine);

   void visitOperation(Operation operation);

   void visitSegment(Segment segment);

   void visitSql(Sql sql);

   void visitStorageReport(StorageReport storageReport);
}
