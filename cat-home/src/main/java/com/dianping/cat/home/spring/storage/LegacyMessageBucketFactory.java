package com.dianping.cat.home.spring.storage;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Component;

import com.dianping.cat.message.storage.LocalMessageBucket;
import com.dianping.cat.message.storage.MessageBucketFactory;

@Component("legacyMessageBucketFactory")
public class LegacyMessageBucketFactory implements MessageBucketFactory {

	@Override
	public LocalMessageBucket createBucket(File baseDir, String dataFile) throws IOException {
		LocalMessageBucket bucket = new LocalMessageBucket();

		bucket.setBaseDir(baseDir);
		bucket.initialize(dataFile);
		return bucket;
	}
}
