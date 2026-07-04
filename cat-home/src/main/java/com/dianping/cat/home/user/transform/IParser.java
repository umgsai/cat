package com.dianping.cat.home.user.transform;

import com.dianping.cat.home.user.entity.User;
import com.dianping.cat.home.user.entity.UserConfig;

public interface IParser<T> {
   UserConfig parse(IMaker<T> maker, ILinker linker, T node);

   void parseForUser(IMaker<T> maker, ILinker linker, User parent, T node);
}
