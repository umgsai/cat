package com.dianping.cat.home.business;

import com.dianping.cat.home.business.entity.BusinessItem;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.home.business.entity.Tag;

public interface IVisitor {

   void visitBusinessItem(BusinessItem businessItem);

   void visitBusinessTagConfig(BusinessTagConfig businessTagConfig);

   void visitTag(Tag tag);
}
