package com.dianping.cat.home.storage.alert;

import com.dianping.cat.home.storage.alert.entity.Detail;
import com.dianping.cat.home.storage.alert.entity.Machine;
import com.dianping.cat.home.storage.alert.entity.Operation;
import com.dianping.cat.home.storage.alert.entity.Storage;
import com.dianping.cat.home.storage.alert.entity.StorageAlertInfo;
import com.dianping.cat.home.storage.alert.entity.Target;

public interface IVisitor {

   void visitDetail(Detail detail);

   void visitMachine(Machine machine);

   void visitOperation(Operation operation);

   void visitStorage(Storage storage);

   void visitStorageAlertInfo(StorageAlertInfo storageAlertInfo);

   void visitTarget(Target target);
}
