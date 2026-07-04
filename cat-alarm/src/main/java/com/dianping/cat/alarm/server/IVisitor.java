package com.dianping.cat.alarm.server;

import com.dianping.cat.alarm.server.entity.Condition;
import com.dianping.cat.alarm.server.entity.Rule;
import com.dianping.cat.alarm.server.entity.ServerAlarmRuleConfig;
import com.dianping.cat.alarm.server.entity.SubCondition;

public interface IVisitor {

   void visitCondition(Condition condition);

   void visitRule(Rule rule);

   void visitServerAlarmRuleConfig(ServerAlarmRuleConfig serverAlarmRuleConfig);

   void visitSubCondition(SubCondition subCondition);
}
