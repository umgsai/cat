package com.dianping.cat.home.spring.storage;

import java.io.IOException;
import java.util.Date;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketFactory;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.hadoop.hdfs.bucket.AbstractHdfsMessageBucket;
import com.dianping.cat.hadoop.hdfs.bucket.HarfsMessageBucket;
import com.dianping.cat.hadoop.hdfs.bucket.HdfsMessageBucket;
import com.dianping.cat.message.storage.MessageBucket;

@Component("hdfsMessageBucketFactory")
public class SpringHdfsMessageBucketFactory implements HdfsMessageBucketFactory {
	@Resource(name = "hdfsLogviewFileSystemManager")
	private com.dianping.cat.hadoop.hdfs.FileSystemManager fileSystemManager;

	@Override
	public MessageBucket createBucket(String type, String dataFile, Date date) throws IOException {
		AbstractHdfsMessageBucket bucket;

		if (HdfsMessageBucketManager.HARFS_BUCKET.equals(type)) {
			bucket = new HarfsMessageBucket();
		} else if (HdfsMessageBucketManager.HDFS_BUCKET.equals(type)) {
			bucket = new HdfsMessageBucket();
		} else {
			throw new IllegalArgumentException("Unsupported HDFS message bucket type: " + type);
		}
		bucket.setFileSystemManager(fileSystemManager);
		bucket.initialize(dataFile, date);
		return bucket;
	}
}
