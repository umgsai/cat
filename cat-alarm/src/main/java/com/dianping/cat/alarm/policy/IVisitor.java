package com.dianping.cat.alarm.policy;

import com.dianping.cat.alarm.policy.entity.AlertPolicy;
import com.dianping.cat.alarm.policy.entity.Group;
import com.dianping.cat.alarm.policy.entity.Level;
import com.dianping.cat.alarm.policy.entity.Type;

public interface IVisitor {

   void visitAlertPolicy(AlertPolicy alertPolicy);

   void visitGroup(Group group);

   void visitLevel(Level level);

   void visitType(Type type);
}
