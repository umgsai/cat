package com.dianping.cat.home.alert.summary.transform;

import com.dianping.cat.home.alert.summary.entity.Alert;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.home.alert.summary.entity.Category;

public interface IMaker<T> {

   Alert buildAlert(T node);

   AlertSummary buildAlertSummary(T node);

   Category buildCategory(T node);
}
