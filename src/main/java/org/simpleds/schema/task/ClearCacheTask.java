package org.simpleds.schema.task;

import java.util.Map;

import org.simpleds.schema.AbstractTask;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/**
 * Delete the memcache contents
 * @author icoloma
 *
 */
public class ClearCacheTask extends AbstractTask {

	public ClearCacheTask() {
		super("clear-cache");
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
