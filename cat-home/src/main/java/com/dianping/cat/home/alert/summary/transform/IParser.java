package com.dianping.cat.home.alert.summary.transform;

import com.dianping.cat.home.alert.summary.entity.Alert;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.home.alert.summary.entity.Category;

public interface IParser<T> {
   AlertSummary parse(IMaker<T> maker, ILinker linker, T node);

   void parseForAlert(IMaker<T> maker, ILinker linker, Alert parent, T node);

   void parseForCategory(IMaker<T> maker, ILinker linker, Category parent, T node);
}
