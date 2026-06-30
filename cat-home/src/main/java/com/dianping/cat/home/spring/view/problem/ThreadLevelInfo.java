package com.dianping.cat.home.spring.view.problem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.report.view.ProblemReportHelper;

public class ThreadLevelInfo {
	private List<String> m_datas = new ArrayList<String>();

	private String m_date;

	private String m_domain;

	private String m_groupName;

	private Map<String, GroupStatistics> m_groupStatistics = new LinkedHashMap<String, GroupStatistics>();

	private String m_ipAddress;

	private int m_minutes;

	private long m_longDate;

	private Map<String, TreeSet<String>> m_threadsInfo = new HashMap<String, TreeSet<String>>();

	public ThreadLevelInfo(String domain, String ipAddress, String date, long longDate, int lastMinute, String groupName) {
		m_date = date;
		m_domain = domain;
		m_groupName = groupName;
		m_ipAddress = ipAddress;
		m_minutes = lastMinute;
		m_longDate = longDate;
	}

	public ThreadLevelInfo display(ProblemReport report) {
		Machine machine = report.getMachines().get(m_ipAddress);
		if (machine == null) {
			return null;
		}

		Collection<Entity> entities = machine.getEntities().values();
		for (Entity temp : entities) {
			Map<String, JavaThread> threads = temp.getThreads();

			for (java.util.Map.Entry<String, JavaThread> entry : threads.entrySet()) {
				JavaThread thread = entry.getValue();
				String groupName = thread.getGroupName();
				String threadId = thread.getId();
				GroupStatistics statistics = findOrCreateGroupStatistics(groupName, m_minutes);

				if (groupName.equals(m_groupName)) {
					statistics.add(threadId, thread.getSegments(), m_minutes, temp.getType());
					findOrCreateThreadInfo(groupName, threadId);
				} else {
					statistics.add(groupName, thread.getSegments(), m_minutes, temp.getType());
					findOrCreateThreadInfo(groupName, groupName);
				}
			}
		}
		long currentTimeMillis = System.currentTimeMillis();
		long currentHours = currentTimeMillis - currentTimeMillis % (60 * 60 * 1000);
		if (currentHours == m_longDate) {
			for (int i = m_minutes; i >= 0; i--) {
				m_datas.add(getShowDetailByMinute(i));
			}
		} else {
			for (int i = 0; i <= m_minutes; i++) {
				m_datas.add(getShowDetailByMinute(i));
			}
		}
		return this;
	}

	public GroupStatistics findOrCreateGroupStatistics(String groupName, int lastMinute) {
		m_minutes = lastMinute;

		GroupStatistics value = m_groupStatistics.get(groupName);
		if (value == null) {
			GroupStatistics result = new GroupStatistics();
			m_groupStatistics.put(groupName, result);
			return result;
		} else {
			return value;
		}
	}

	public List<String> getDatas() {
		return m_datas;
	}

	public List<GroupDisplayInfo> getGroups() {
		List<GroupDisplayInfo> result = new ArrayList<GroupDisplayInfo>();

		for (java.util.Map.Entry<String, TreeSet<String>> entry : m_threadsInfo.entrySet()) {
			result.add(new GroupDisplayInfo().setName(entry.getKey()).setNumber(entry.getValue().size()));
		}
		Collections.sort(result, new Comparator<GroupDisplayInfo>() {
			@Override
			public int compare(GroupDisplayInfo o1, GroupDisplayInfo o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return result;
	}

	public List<String> getThreads() {
		List<String> result = new ArrayList<String>();

		for (TreeSet<String> set : m_threadsInfo.values()) {
			for (String thread : set) {
				result.add(thread);
			}
		}
		return result;
	}

	private void findOrCreateThreadInfo(String groupName, String threadName) {
		TreeSet<String> sets = m_threadsInfo.get(groupName);

		if (sets != null) {
			sets.add(threadName);
		} else {
			sets = new TreeSet<String>();

			sets.add(threadName);
			m_threadsInfo.put(groupName, sets);
		}
	}

	private String getDisplayHour() {
		return m_date.substring(8, 10);
	}

	private String getShowDetailByMinute(int minute) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		String baseUrl = "/cat/mvc/r/p?op=detail";
		params.put("domain", m_domain);
		params.put("ip", m_ipAddress);
		params.put("date", m_date);
		params.put("minute", Integer.toString(minute));

		StringBuilder sb = new StringBuilder().append("<td>");
		String minuteStr = getDisplayHour() + ":";
		if (minute < 10) {
			minuteStr = minuteStr + "0" + Integer.toString(minute);
		} else {
			minuteStr = minuteStr + Integer.toString(minute);
		}
		sb.append(ProblemReportHelper.creatLinkString(baseUrl, "minute", params, minuteStr));
		sb.append("</td>");

		for (GroupDisplayInfo group : getGroups()) {
			String groupName = group.getName();
			GroupStatistics value = m_groupStatistics.get(groupName);
			Set<String> threads = getThreadsByGroup(groupName);
			Map<String, ThreadStatistics> temps = value.getStatistics();

			for (String thread : threads) {
				ThreadStatistics threadStatistics = temps.get(thread);
				TreeSet<String> errors = threadStatistics.getStatistics().get(minute);
				sb.append("<td>");
				for (String error : errors) {
					params.put("group", groupName);
					if (groupName.equals(m_groupName)) {
						params.put("thread", thread);
					}
					String url = ProblemReportHelper.creatLinkString(baseUrl, error, params, "");
					sb.append(url);
				}
				sb.append("</td>");
			}
		}
		return sb.toString();
	}

	private TreeSet<String> getThreadsByGroup(String groupName) {
		return m_threadsInfo.get(groupName);
	}

	public static class GroupDisplayInfo {
		private String m_name;

		private int m_number;

		public String getName() {
			return m_name;
		}

		public GroupDisplayInfo setName(String name) {
			m_name = name;
			return this;
		}

		public int getNumber() {
			return m_number;
		}

		public GroupDisplayInfo setNumber(int number) {
			m_number = number;
			return this;
		}
	}

	public static class GroupStatistics {
		private Map<String, ThreadStatistics> m_statistics = new LinkedHashMap<String, ThreadStatistics>();

		public void add(String threadId, Map<Integer, Segment> segments, int minute, String type) {
			findOrCreateThreadStatistics(threadId, minute).add(segments, type);
		}

		public ThreadStatistics findOrCreateThreadStatistics(String threadName, int minute) {
			ThreadStatistics statistics = m_statistics.get(threadName);
			if (statistics == null) {
				ThreadStatistics result = new ThreadStatistics(minute);

				m_statistics.put(threadName, result);
				return result;
			} else {
				return statistics;
			}
		}

		public Map<String, ThreadStatistics> getStatistics() {
			return m_statistics;
		}

		public void setStatistics(Map<String, ThreadStatistics> statistics) {
			m_statistics = statistics;
		}
	}

	public static class ThreadStatistics {
		private Map<Integer, TreeSet<String>> m_statistics = new LinkedHashMap<Integer, TreeSet<String>>();

		public ThreadStatistics(int lastMinute) {
			for (int i = 0; i <= lastMinute; i++) {
				m_statistics.put(i, new TreeSet<String>());
			}
		}

		public void add(Map<Integer, Segment> segments, String type) {
			for (java.util.Map.Entry<Integer, Segment> entry : segments.entrySet()) {
				findOrCreate(entry.getKey()).add(type);
			}
		}

		public TreeSet<String> findOrCreate(Integer key) {
			TreeSet<String> result = m_statistics.get(key);
			if (result == null) {
				result = new TreeSet<String>();
				m_statistics.put(key, result);
			}
			return result;
		}

		public Map<Integer, TreeSet<String>> getStatistics() {
			return m_statistics;
		}
	}
}
