package com.dianping.cat.alarm.rule;

import com.dianping.cat.alarm.rule.entity.Condition;
import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.rule.entity.MetricItem;
import com.dianping.cat.alarm.rule.entity.MonitorRules;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.entity.SubCondition;

public interface IVisitor {

   void visitCondition(Condition condition);

   void visitConfig(Config config);

   void visitMetricItem(MetricItem metricItem);

   void visitMonitorRules(MonitorRules monitorRules);

   void visitRule(Rule rule);

   void visitSubCondition(SubCondition subCondition);
}
