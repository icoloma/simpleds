package org.simpleds.bg.tasks;

import org.simpleds.bg.AbstractBackgroundTask;
import org.simpleds.bg.TaskRequest;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterator;

/**
 * Process Datastore entities one by one
 * 
 * @author icoloma
 *
 */
public abstract class IterableEntityTask extends AbstractBackgroundTask {

	public IterableEntityTask(String id) {
		super(id);
	}
	
	@Override
	public long doProceed(TaskRequest request) {
		// execute the query and locate the cursor, if any
		Query query = createQuery(request);
		query.setKeysOnly();
		PreparedQuery pq = datastoreService.prepare(query);
		
		QueryResultIterator<Entity> it = pq.asQueryResultIterator(createFetchOptions(request));
		int count = 0;
		while (it.hasNext()) {
			process(it.next(), request);
			count++;
		}
		
		// postpone
		if (count == batchSize) {
			deferProceed(request.withCursor(it.getCursor()));
		} else {
			notifyFinalization();
		}
		return count;
	}

	/**
	 * Process one {@link Entity}
	 */
	protected abstract void process(Entity entity, TaskRequest request);

	/**
	 * Create the query that will return the list of entities to process.
	 * @param request the current task request
	 */
	protected abstract Query createQuery(TaskRequest request);
	
}