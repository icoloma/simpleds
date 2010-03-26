package org.simpleds.schema.action;

import java.util.Map;

import org.simpleds.schema.AbstractDatastoreAction;

import com.google.appengine.api.datastore.Key;

/**
 * Simple example to delete by primary key.
 * This action will ignore the batchsize attribute and just try to delete all the keys at once.
 * @author icoloma
 *
 */
public class DeleteByKeysAction extends AbstractDatastoreAction {

	protected Key[] keys;
	
	public DeleteByKeysAction(String id, Key... keys) {
		super(id);
		this.keys = keys;
	}
	
	@Override
	public long proceed(String uri, Map<String, String> params) {
		datastoreService.delete(keys);
		doNestedActions(uri, params);
		return keys.length;
	}
	
}
