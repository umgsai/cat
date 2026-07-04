package org.unidal.cat.message.storage;

import java.io.IOException;

public interface IndexFactory {

	Index createIndex(String domain, String ip, int hour) throws IOException;
}
