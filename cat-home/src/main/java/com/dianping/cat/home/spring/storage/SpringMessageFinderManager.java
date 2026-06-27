package com.dianping.cat.home.spring.storage;

import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.internals.DefaultMessageFinderManager;

@Component("messageFinderManager")
public class SpringMessageFinderManager extends DefaultMessageFinderManager {
}
