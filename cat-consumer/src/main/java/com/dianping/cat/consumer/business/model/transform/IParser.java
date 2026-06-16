package com.dianping.cat.consumer.business.model.transform;

import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;

public interface IParser<T> {
   public BusinessReport parse(IMaker<T> maker, ILinker linker, T node);

   public void parseForBusinessItem(IMaker<T> maker, ILinker linker, BusinessItem parent, T node);

   public void parseForSegment(IMaker<T> maker, ILinker linker, Segment parent, T node);
}
