package com.dianping.cat.home.business.transform;

import com.dianping.cat.home.business.entity.BusinessItem;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.home.business.entity.Tag;

public interface ILinker {

   boolean onBusinessItem(Tag parent, BusinessItem businessItem);

   boolean onTag(BusinessTagConfig parent, Tag tag);
}
