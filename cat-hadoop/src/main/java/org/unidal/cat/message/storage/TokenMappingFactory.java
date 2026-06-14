package org.unidal.cat.message.storage;

import java.io.IOException;

public interface TokenMappingFactory {

	public TokenMapping createTokenMapping(int hour, String ip) throws IOException;
}
