package com.dianping.cat.home.business.transform;

import com.dianping.cat.home.business.entity.BusinessItem;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.home.business.entity.Tag;

public interface IMaker<T> {

   BusinessItem buildBusinessItem(T node);

   BusinessTagConfig buildBusinessTagConfig(T node);

   Tag buildTag(T node);
}
