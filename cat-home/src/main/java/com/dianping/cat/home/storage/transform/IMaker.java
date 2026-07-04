package com.dianping.cat.home.storage.transform;

import com.dianping.cat.home.storage.entity.Link;
import com.dianping.cat.home.storage.entity.Storage;
import com.dianping.cat.home.storage.entity.StorageGroup;
import com.dianping.cat.home.storage.entity.StorageGroupConfig;

public interface IMaker<T> {

   Link buildLink(T node);

   String buildPar(T node);

   Storage buildStorage(T node);

   StorageGroup buildStorageGroup(T node);

   StorageGroupConfig buildStorageGroupConfig(T node);
}
