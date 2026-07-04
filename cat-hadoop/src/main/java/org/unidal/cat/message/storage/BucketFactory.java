package org.unidal.cat.message.storage;

import java.io.IOException;

public interface BucketFactory {

	Bucket createBucket(String domain, String ip, int hour, boolean writeMode) throws IOException;
}
