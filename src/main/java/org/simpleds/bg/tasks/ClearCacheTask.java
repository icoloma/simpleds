package org.simpleds.bg.tasks;

import java.util.Map;

import org.simpleds.bg.AbstractBackgroundTask;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/**
 * Delete the memcache contents. 
 * @author icoloma
 *
 */
public class ClearCacheTask extends AbstractBackgroundTask {
	
	/** the dafult ID of this task */
	public static final String DEFAULT_ID = "clear-cache";

	/**
	 * Creates a new instance with id=ClearCacheTask.DEFAULT_ID
	 */
	public ClearCacheTask() {
		super(DEFAULT_ID);
	}
	
	private ClearCacheTask(String id) {
		super(id);
	}

	@Override
	public long doProceed(String uri, Map<String, String> params) {
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		long items = memcache.getStatistics().getItemCount();
		memcache.clearAll();
		doNestedTasks(uri, params);
		return items;
	}
	
}
