package com.dianping.cat.alarm.rule.transform;

import com.dianping.cat.alarm.rule.entity.Condition;
import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.rule.entity.MetricItem;
import com.dianping.cat.alarm.rule.entity.MonitorRules;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.entity.SubCondition;

public interface ILinker {

   boolean onCondition(Config parent, Condition condition);

   boolean onConfig(Rule parent, Config config);

   boolean onMetricItem(Rule parent, MetricItem metricItem);

   boolean onRule(MonitorRules parent, Rule rule);

   boolean onSubCondition(Condition parent, SubCondition subCondition);
}
