package com.dianping.cat.configuration.web.url.transform;

import com.dianping.cat.configuration.web.url.entity.Code;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.configuration.web.url.entity.UrlPattern;

public interface IMaker<T> {

   Code buildCode(T node);

   PatternItem buildPatternItem(T node);

   UrlPattern buildUrlPattern(T node);
}
