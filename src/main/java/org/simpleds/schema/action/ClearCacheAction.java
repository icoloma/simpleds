package org.simpleds.schema.action;

import java.util.Map;

import org.simpleds.schema.AbstractDatastoreAction;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/**
 * Delete the memcache contents
 * @author icoloma
 *
 */
public class ClearCacheAction extends AbstractDatastoreAction {

	public ClearCacheAction() {
		super("clear-cache");
	}
	
	private ClearCacheAction(String id) {
		super(id);
	}

	@Override
	public long proceed(String uri, Map<String, String> params) {
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		long items = memcache.getStatistics().getItemCount();
		memcache.clearAll();
		doNestedActions(uri, params);
		return items;
	}
	
}
