package com.dianping.cat.message.storage;

import java.io.File;
import java.io.IOException;

public interface MessageBucketFactory {

	LocalMessageBucket createBucket(File baseDir, String dataFile) throws IOException;
}
