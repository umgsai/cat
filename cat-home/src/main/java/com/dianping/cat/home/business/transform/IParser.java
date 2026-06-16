package com.dianping.cat.home.business.transform;

import com.dianping.cat.home.business.entity.BusinessItem;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.home.business.entity.Tag;

public interface IParser<T> {
   public BusinessTagConfig parse(IMaker<T> maker, ILinker linker, T node);

   public void parseForBusinessItem(IMaker<T> maker, ILinker linker, BusinessItem parent, T node);

   public void parseForTag(IMaker<T> maker, ILinker linker, Tag parent, T node);
}
