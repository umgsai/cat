package org.unidal.cat.message.storage;

import java.io.IOException;

public interface IndexFactory {

	public Index createIndex(String domain, String ip, int hour) throws IOException;
}
