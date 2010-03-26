package org.simpleds.schema.action;

import java.util.Map;

import org.simpleds.schema.AbstractDatastoreAction;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterator;

/**
 * Process entities, one at a time
 * @author icoloma
 *
 */
public abstract class IterableAction extends AbstractDatastoreAction {

	public IterableAction(String id) {
		super(id);
	}
	
	@Override
	public long proceed(String uri, Map<String, String> params) {
		// execute the query and locate the cursor, if any
		Query query = createQuery(params);
		query.setKeysOnly();
		PreparedQuery pq = datastoreService.prepare(query);
		
		QueryResultIterator<Entity> it = pq.asQueryResultIterator(createFetchOptions(params));
		int count = 0;
		while (it.hasNext()) {
			update(it.next());
			count++;
		}
		
		// postpone
		if (count == batchSize) {
			deferProceed(it.getCursor(), uri, params);
		} else {
			doNestedActions(uri, params);
		}
		return count;
	}

	/**
	 * Update and store the entity
	 */
	protected abstract void update(Entity entity);

	/**
	 * Create the query according to the proposed params
	 */
	protected abstract Query createQuery(Map<String, String> params);
	
}
