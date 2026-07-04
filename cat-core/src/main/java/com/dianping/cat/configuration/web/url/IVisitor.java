package com.dianping.cat.configuration.web.url;

import com.dianping.cat.configuration.web.url.entity.Code;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.configuration.web.url.entity.UrlPattern;

public interface IVisitor {

   void visitCode(Code code);

   void visitPatternItem(PatternItem patternItem);

   void visitUrlPattern(UrlPattern urlPattern);
}
