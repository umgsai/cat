package com.dianping.cat.configuration.web.url.transform;

import com.dianping.cat.configuration.web.url.entity.Code;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.configuration.web.url.entity.UrlPattern;

public interface IParser<T> {
   UrlPattern parse(IMaker<T> maker, ILinker linker, T node);

   void parseForCode(IMaker<T> maker, ILinker linker, Code parent, T node);

   void parseForPatternItem(IMaker<T> maker, ILinker linker, PatternItem parent, T node);
}
