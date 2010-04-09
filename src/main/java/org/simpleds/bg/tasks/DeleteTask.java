package org.simpleds.bg.tasks;

import java.util.List;

import org.simpleds.bg.AbstractBackgroundTask;
import org.simpleds.bg.TaskRequest;

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
public abstract class DeleteTask extends AbstractBackgroundTask {

	public DeleteTask(String id) {
		super(id);
	}
	
	@Override
	public long doProceed(TaskRequest request) {
		// execute the query and locate the cursor, if any
		Query query = createQuery(request);
		query.setKeysOnly();
		PreparedQuery pq = datastoreService.prepare(query);
		
		// retrieve the primary keys
		List<Key> keys = Lists.newArrayListWithExpectedSize(batchSize);
		QueryResultIterator<Entity> it = pq.asQueryResultIterator(createFetchOptions(request));
		while (it.hasNext()) {
			keys.add(it.next().getKey());
		}
		datastoreService.delete(keys);
		
		// postpone
		if (keys.size() == batchSize) {
			deferProceed(request.withCursor(it.getCursor()));
		} else {
			notifyFinalization();
		}
		return keys.size();
	}

	/**
	 * Create the query according to the proposed params
	 */
	protected abstract Query createQuery(TaskRequest request);
	
}
