package com.dianping.cat.home.user;

import com.dianping.cat.home.user.entity.User;
import com.dianping.cat.home.user.entity.UserConfig;

public interface IVisitor {

   public void visitUser(User user);

   public void visitUserConfig(UserConfig userConfig);
}
