package com.dianping.cat.home.business;

import com.dianping.cat.home.business.entity.BusinessItem;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.home.business.entity.Tag;

public interface IVisitor {

   public void visitBusinessItem(BusinessItem businessItem);

   public void visitBusinessTagConfig(BusinessTagConfig businessTagConfig);

   public void visitTag(Tag tag);
}
