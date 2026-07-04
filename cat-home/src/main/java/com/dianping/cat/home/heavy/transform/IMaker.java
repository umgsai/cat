package com.dianping.cat.home.heavy.transform;

import com.dianping.cat.home.heavy.entity.HeavyCache;
import com.dianping.cat.home.heavy.entity.HeavyCall;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.heavy.entity.HeavySql;
import com.dianping.cat.home.heavy.entity.Service;
import com.dianping.cat.home.heavy.entity.Url;

public interface IMaker<T> {

   HeavyCache buildHeavyCache(T node);

   HeavyCall buildHeavyCall(T node);

   HeavyReport buildHeavyReport(T node);

   HeavySql buildHeavySql(T node);

   Service buildService(T node);

   Url buildUrl(T node);
}
