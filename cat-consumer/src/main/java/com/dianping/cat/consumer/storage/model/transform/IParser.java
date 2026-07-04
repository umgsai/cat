package com.dianping.cat.consumer.storage.model.transform;

import com.dianping.cat.consumer.storage.model.entity.Domain;
import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.entity.Segment;
import com.dianping.cat.consumer.storage.model.entity.Sql;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;

public interface IParser<T> {
   StorageReport parse(IMaker<T> maker, ILinker linker, T node);

   void parseForDomain(IMaker<T> maker, ILinker linker, Domain parent, T node);

   void parseForMachine(IMaker<T> maker, ILinker linker, Machine parent, T node);

   void parseForOperation(IMaker<T> maker, ILinker linker, Operation parent, T node);

   void parseForSegment(IMaker<T> maker, ILinker linker, Segment parent, T node);

   void parseForSql(IMaker<T> maker, ILinker linker, Sql parent, T node);
}
