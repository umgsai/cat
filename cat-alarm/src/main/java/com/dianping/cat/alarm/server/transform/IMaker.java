package com.dianping.cat.alarm.server.transform;

import com.dianping.cat.alarm.server.entity.Condition;
import com.dianping.cat.alarm.server.entity.Rule;
import com.dianping.cat.alarm.server.entity.ServerAlarmRuleConfig;
import com.dianping.cat.alarm.server.entity.SubCondition;

public interface IMaker<T> {

   Condition buildCondition(T node);

   Rule buildRule(T node);

   ServerAlarmRuleConfig buildServerAlarmRuleConfig(T node);

   SubCondition buildSubCondition(T node);
}
