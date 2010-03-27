package org.simpleds.schema.task;

import java.util.Map;

import org.simpleds.schema.AbstractTask;

import com.google.appengine.api.datastore.Key;

/**
 * Simple example to delete by primary key.
 * This action will ignore the batchsize attribute and just try to delete all the keys at once.
 * @author icoloma
 *
 */
public class DeleteByKeysAction extends AbstractTask {

	protected Key[] keys;
	
	public DeleteByKeysAction(String id, Key... keys) {
		super(id);
		this.keys = keys;
	}
	
	@Override
	public long doProceed(String uri, Map<String, String> params) {
		datastoreService.delete(keys);
		doNestedTasks(uri, params);
		return keys.length;
	}
	
}
