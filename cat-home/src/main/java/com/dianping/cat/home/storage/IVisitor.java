package com.dianping.cat.home.storage;

import com.dianping.cat.home.storage.entity.Link;
import com.dianping.cat.home.storage.entity.Storage;
import com.dianping.cat.home.storage.entity.StorageGroup;
import com.dianping.cat.home.storage.entity.StorageGroupConfig;

public interface IVisitor {

   void visitLink(Link link);

   void visitStorage(Storage storage);

   void visitStorageGroup(StorageGroup storageGroup);

   void visitStorageGroupConfig(StorageGroupConfig storageGroupConfig);
}
