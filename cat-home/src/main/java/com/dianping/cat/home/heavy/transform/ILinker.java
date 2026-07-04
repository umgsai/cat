package com.dianping.cat.home.heavy.transform;

import com.dianping.cat.home.heavy.entity.HeavyCache;
import com.dianping.cat.home.heavy.entity.HeavyCall;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.heavy.entity.HeavySql;
import com.dianping.cat.home.heavy.entity.Service;
import com.dianping.cat.home.heavy.entity.Url;

public interface ILinker {

   boolean onHeavyCache(HeavyReport parent, HeavyCache heavyCache);

   boolean onHeavyCall(HeavyReport parent, HeavyCall heavyCall);

   boolean onHeavySql(HeavyReport parent, HeavySql heavySql);

   boolean onService(HeavySql parent, Service service);

   boolean onService(HeavyCall parent, Service service);

   boolean onService(HeavyCache parent, Service service);

   boolean onUrl(HeavySql parent, Url url);

   boolean onUrl(HeavyCall parent, Url url);

   boolean onUrl(HeavyCache parent, Url url);
}
