package org.simpleds.schema;

import java.util.List;
import java.util.Map;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.collect.ImmutableList;

/**
 * Stores and retrieves the results of ongoing tasks inside memcache 
 * @author icoloma
 *
 */
public class TaskStats {
	
	/** timestamp of start, in millis */
	private static final String START = "start-";
	
	/** total number of entities processed */
	private static final String ENTITIES = "entities-";
	
	/** total number of batches processed */
	private static final String BATCHES = "batches-";
	
	/** timestamp of task end, in millis */
	private static final String END = "end-";

	/** the namespaced memcache instance to use */
	private MemcacheService memcache;
	
	private static TaskStats instance;
	
	TaskStats() {
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		memcache.setNamespace("_simpleds-stats");
	}
	
	public static TaskStats getInstance() {
		if (instance == null) {
			instance = new TaskStats();
		}
		return instance;
	}
	
	public Map<String, Something> getStats() {
	}
	
	public void start(String path) {
		memcache.put(START + path, System.currentTimeMillis());
		List keys = ImmutableList.of(
			ENTITIES + path, BATCHES + path, END + path
		);
		memcache.deleteAll(keys);
	}
	
	public void end(String path) {
		memcache.put(END + path, System.currentTimeMillis());
	}
	
	public void addResults(String path, long entitiesProcessed) {
		memcache.increment(END + path, entitiesProcessed, 0L);
		memcache.increment(BATCHES + path, 1, 0L);
	}
	
	/**
	 * 
	 * @param path
	 * @param entitiesProcessed
	 */
	public static void notifyResults(String path, long entitiesProcessed) {
		TaskStats status = new TaskStats();
		status.put(path, entitiesProcessed);
	}

}
