package com.dianping.cat.hadoop.hdfs;

import java.io.IOException;
import java.util.Date;

import com.dianping.cat.message.storage.MessageBucket;

public interface HdfsMessageBucketFactory {

	MessageBucket createBucket(String type, String dataFile, Date date) throws IOException;
}
