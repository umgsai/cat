package com.dianping.cat.home.alert.summary;

import com.dianping.cat.home.alert.summary.entity.Alert;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.home.alert.summary.entity.Category;

public interface IVisitor {

   void visitAlert(Alert alert);

   void visitAlertSummary(AlertSummary alertSummary);

   void visitCategory(Category category);
}
