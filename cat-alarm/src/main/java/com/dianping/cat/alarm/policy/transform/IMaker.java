package com.dianping.cat.alarm.policy.transform;

import com.dianping.cat.alarm.policy.entity.AlertPolicy;
import com.dianping.cat.alarm.policy.entity.Group;
import com.dianping.cat.alarm.policy.entity.Level;
import com.dianping.cat.alarm.policy.entity.Type;

public interface IMaker<T> {

   AlertPolicy buildAlertPolicy(T node);

   Group buildGroup(T node);

   Level buildLevel(T node);

   Type buildType(T node);
}
