package com.dianping.cat.home.user.transform;

import com.dianping.cat.home.user.entity.User;
import com.dianping.cat.home.user.entity.UserConfig;

public interface ILinker {

   public boolean onUser(UserConfig parent, User user);
}
