package com.dianping.cat.alarm.rule.transform;

import com.dianping.cat.alarm.rule.entity.Condition;
import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.rule.entity.MetricItem;
import com.dianping.cat.alarm.rule.entity.MonitorRules;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.entity.SubCondition;

public interface IMaker<T> {

   Condition buildCondition(T node);

   Config buildConfig(T node);

   MetricItem buildMetricItem(T node);

   MonitorRules buildMonitorRules(T node);

   Rule buildRule(T node);

   SubCondition buildSubCondition(T node);
}
