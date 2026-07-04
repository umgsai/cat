package com.dianping.cat.home.user;

import com.dianping.cat.home.user.entity.User;
import com.dianping.cat.home.user.entity.UserConfig;

public interface IVisitor {

   void visitUser(User user);

   void visitUserConfig(UserConfig userConfig);
}
