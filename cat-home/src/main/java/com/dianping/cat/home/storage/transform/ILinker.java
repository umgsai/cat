package com.dianping.cat.home.storage.transform;

import com.dianping.cat.home.storage.entity.Link;
import com.dianping.cat.home.storage.entity.Storage;
import com.dianping.cat.home.storage.entity.StorageGroup;
import com.dianping.cat.home.storage.entity.StorageGroupConfig;

public interface ILinker {

   boolean onLink(StorageGroup parent, Link link);

   boolean onStorage(StorageGroup parent, Storage storage);

   boolean onStorageGroup(StorageGroupConfig parent, StorageGroup storageGroup);
}
