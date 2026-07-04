package com.dianping.cat.home.heavy;

import com.dianping.cat.home.heavy.entity.HeavyCache;
import com.dianping.cat.home.heavy.entity.HeavyCall;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.heavy.entity.HeavySql;
import com.dianping.cat.home.heavy.entity.Service;
import com.dianping.cat.home.heavy.entity.Url;

public interface IVisitor {

   void visitHeavyCache(HeavyCache heavyCache);

   void visitHeavyCall(HeavyCall heavyCall);

   void visitHeavyReport(HeavyReport heavyReport);

   void visitHeavySql(HeavySql heavySql);

   void visitService(Service service);

   void visitUrl(Url url);
}
