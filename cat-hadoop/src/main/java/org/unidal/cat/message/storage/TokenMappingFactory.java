package org.unidal.cat.message.storage;

import java.io.IOException;

public interface TokenMappingFactory {

	TokenMapping createTokenMapping(int hour, String ip) throws IOException;
}
