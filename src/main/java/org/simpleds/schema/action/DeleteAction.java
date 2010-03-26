package org.simpleds.schema.action;

import java.util.List;
import java.util.Map;

import org.simpleds.schema.AbstractDatastoreAction;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.common.collect.Lists;

/**
 * Delete all entities returned by a query. This class will use batch operations to improve efficiency,
 * and applies a default batchSize value of 500.
 * @author icoloma
 *
 */
public abstract class DeleteAction extends AbstractDatastoreAction {

	public DeleteAction(String id) {
		super(id);
		withBatchSize(500);
	}
	
	@Override
	public long proceed(String uri, Map<String, String> params) {
		// execute the query and locate the cursor, if any
		Query query = createQuery(params);
		query.setKeysOnly();
		PreparedQuery pq = datastoreService.prepare(query);
		
		// retrieve the primary keys
		List<Key> keys = Lists.newArrayListWithExpectedSize(batchSize);
		QueryResultIterator<Entity> it = pq.asQueryResultIterator(createFetchOptions(params));
		while (it.hasNext()) {
			keys.add(it.next().getKey());
		}
		datastoreService.delete(keys);
		
		// postpone
		if (keys.size() == batchSize) {
			deferProceed(it.getCursor(), uri, params);
		} else {
			doNestedActions(uri, params);
		}
		return keys.size();
	}

	/**
	 * Create the query according to the proposed params
	 */
	protected abstract Query createQuery(Map<String, String> params);
	
}
