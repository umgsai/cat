package com.dianping.cat.alarm.server.transform;

import com.dianping.cat.alarm.server.entity.Condition;
import com.dianping.cat.alarm.server.entity.Rule;
import com.dianping.cat.alarm.server.entity.ServerAlarmRuleConfig;
import com.dianping.cat.alarm.server.entity.SubCondition;

public interface IParser<T> {
   ServerAlarmRuleConfig parse(IMaker<T> maker, ILinker linker, T node);

   void parseForCondition(IMaker<T> maker, ILinker linker, Condition parent, T node);

   void parseForRule(IMaker<T> maker, ILinker linker, Rule parent, T node);

   void parseForSubCondition(IMaker<T> maker, ILinker linker, SubCondition parent, T node);
}
