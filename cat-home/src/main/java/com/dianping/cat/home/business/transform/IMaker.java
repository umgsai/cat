package com.dianping.cat.home.business.transform;

import com.dianping.cat.home.business.entity.BusinessItem;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.home.business.entity.Tag;

public interface IMaker<T> {

   public BusinessItem buildBusinessItem(T node);

   public BusinessTagConfig buildBusinessTagConfig(T node);

   public Tag buildTag(T node);
}
