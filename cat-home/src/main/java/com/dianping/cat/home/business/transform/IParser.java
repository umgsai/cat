package com.dianping.cat.home.business.transform;

import com.dianping.cat.home.business.entity.BusinessItem;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.home.business.entity.Tag;

public interface IParser<T> {
   BusinessTagConfig parse(IMaker<T> maker, ILinker linker, T node);

   void parseForBusinessItem(IMaker<T> maker, ILinker linker, BusinessItem parent, T node);

   void parseForTag(IMaker<T> maker, ILinker linker, Tag parent, T node);
}
