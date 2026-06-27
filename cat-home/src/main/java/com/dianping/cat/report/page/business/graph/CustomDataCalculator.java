/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.report.page.business.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.report.page.business.task.BusinessKeyHelper;

import jakarta.annotation.Resource;

@Component
public class CustomDataCalculator {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomDataCalculator.class);

	private static final String START = "${";

	private static final String END = "}";

	private static final String SPLITTER = ",";

	private final JexlEngine jexl = new JexlBuilder().cache(512).strict(true).silent(false).create();

	@Resource
	private BusinessKeyHelper businessKeyHelper;

	public List<CustomInfo> translatePattern(String pattern) {
		List<CustomInfo> infos = new ArrayList<CustomInfo>();
		boolean result = true;
		int length = pattern.length();
		int start = -1;
		int end = -1;

		do {
			start = pattern.indexOf(START, end + 1);
			end = pattern.indexOf(END, end + 1);

			if (start >= 0 && end > 0 && start < end) {
				CustomInfo customInfo = new CustomInfo();

				String subPattern = pattern.substring(start + 2, end);
				String[] parts = subPattern.split(SPLITTER);

				if (parts.length == 3) {
					customInfo.setDomain(parts[0].trim());
					customInfo.setKey(parts[1].trim());
					customInfo.setType(parts[2].trim().toUpperCase());
					customInfo.setPattern(pattern.substring(start, end + 1));

					infos.add(customInfo);
				} else {
					result = false;
				}
			} else {
				if (!(start < 0 && end < 0)) {
					result = false;
				}
			}
		} while (end >= 0 && start >= 0 && end < length);

		if (!result) {
			throw new RuntimeException("Wrong Business Pattern!");
		}

		return infos;
	}

	public double[] calculate(String pattern, List<CustomInfo> customInfos, Map<String, double[]> businessItemData,
							int totalSize) {
		double[] result = new double[totalSize];

		for (int i = 0; i < totalSize; i++) {
			try {
				String expression = pattern;

				for (CustomInfo customInfo : customInfos) {
					String customPattern = customInfo.getPattern();
					String itemId = businessKeyHelper.generateKey(customInfo.getKey(), customInfo.getDomain(),
							customInfo.getType());
					double[] sourceData = businessItemData.get(itemId);

					if (sourceData != null) {
						expression = expression.replace(customPattern, Double.toString(sourceData[i]));
					}
				}

				result[i] = calculate(expression);
			} catch (JexlException ex) {
				LOGGER.debug("Unable to calculate business custom expression, index={}, pattern={}.", i, pattern, ex);
			} catch (Exception e) {
				LOGGER.warn("Unable to calculate business custom data, index={}, pattern={}.", i, pattern, e);
				Cat.logError(e);
			}
		}
		return result;
	}

	private double calculate(String pattern) {
		JexlExpression e = jexl.createExpression(pattern);
		Number result = (Number) e.evaluate(null);
		return result.doubleValue();
	}

}
