package com.dianping.cat.home.user.transform;

import com.dianping.cat.home.user.entity.User;
import com.dianping.cat.home.user.entity.UserConfig;

public interface IMaker<T> {

   User buildUser(T node);

   UserConfig buildUserConfig(T node);
}
