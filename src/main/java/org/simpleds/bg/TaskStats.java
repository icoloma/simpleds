package org.simpleds.bg;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

/**
 * Stores and retrieves the results of ongoing tasks inside memcache 
 * @author icoloma
 *
 */
public class TaskStats {
	
	/** timestamp of start, in millis */
	private static final String START = "start-";
	
	/** timestamp of task end, in millis */
	private static final String END = "end-";
	
	/** total number of batches processed */
	private static final String EXECUTIONS = "executions-";
	
	/** total number of entities processed */
	private static final String ENTITIES = "entities-";
	
	private static Log log = LogFactory.getLog(TaskStats.class);

	/** task path */
	private String path;
	
	/** start timestamp */
	private Date start;
	
	/** end timestamp (null if the task is still running) */
	private Date end;
	
	/** number of executed batches */
	private Long executionCount;
	
	/** number of processed entities */
	private Long entityCount;
	
	TaskStats(String path) {
		MemcacheService memcache = getMemcache();
		this.path = path;
		Long l = (Long) memcache.get(START + path);
		if (l != null) {
			this.start = new Date(l);
		}
		l = (Long) memcache.get(END + path);
		if (l != null) {
			this.end = new Date(l);
		}
		this.executionCount = (Long) memcache.get(EXECUTIONS + path); 
		this.entityCount = (Long) memcache.get(ENTITIES + path); 
	}

	private static MemcacheService getMemcache() {
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		memcache.setNamespace("_simpleds-stats");
		return memcache;
	}
	
	static Collection<TaskStats> getTaskStats(Iterable<String> paths) {
		Set<TaskStats> result = Sets.newTreeSet(new TaskStatsComparator());
		for (String path : paths) {
			result.add(new TaskStats(path));
		}
		return result;
	}
	
	public static void start(BackgroundTask task) {
		String path = task.getPath();
		log.info("Entering task: " + path);
		MemcacheService memcache = getMemcache();
		memcache.put(START + path, System.currentTimeMillis(), null, SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
		List keys = ImmutableList.of(
			ENTITIES + path, EXECUTIONS + path, END + path
		);
		memcache.deleteAll(keys);
	}
	
	public static void end(BackgroundTask task) {
		String path = task.getPath();
		log.info("Task " + path + " completed.");
		getMemcache().put(END + path, System.currentTimeMillis());
	}
	
	public static void addResults(BackgroundTask task, long entitiesProcessed) {
		String path = task.getPath();
		log.info(path + " processed " + entitiesProcessed + " entities");
		MemcacheService memcache = getMemcache();
		memcache.increment(ENTITIES + path, entitiesProcessed, 0L);
		memcache.increment(EXECUTIONS + path, 1, 0L);
	}
	
	public String getPath() {
		return path;
	}

	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}

	public Long getExecutionCount() {
		return executionCount;
	}

	public Long getEntityCount() {
		return entityCount;
	}
	
	private static class TaskStatsComparator implements Comparator<TaskStats> {

		@Override
		public int compare(TaskStats t1, TaskStats t2) {
			return t1.getPath().compareTo(t2.getPath());
		}
		
	}

}
