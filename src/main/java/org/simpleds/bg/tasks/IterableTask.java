package org.simpleds.bg.tasks;

import org.simpleds.EntityManager;
import org.simpleds.EntityManagerFactory;
import org.simpleds.SimpleQuery;
import org.simpleds.CursorIterator;
import org.simpleds.bg.AbstractBackgroundTask;
import org.simpleds.bg.TaskRequest;

/**
 * Process SimpleDS entities one by one.
 * 
 * @author icoloma
 *
 */
public abstract class IterableTask<T> extends AbstractBackgroundTask {

	protected EntityManager entityManager;
	
	public IterableTask(String id) {
		super(id);
	}
	
	@Override
	public long doProceed(TaskRequest request) {
		if (entityManager == null) {
			entityManager = EntityManagerFactory.getEntityManager();
		}
		
		// execute the query and locate the cursor, if any
		SimpleQuery query = createQuery(request).withFetchOptions(createFetchOptions(request));
		
		CursorIterator<T> it = entityManager.asIterator(query);
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
	 * Process one entity.
	 */
	protected abstract void process(T entity, TaskRequest request);

	/**
	 * Create the query that will return the list of entities to process.
	 * @param request the current task request
	 */
	protected abstract SimpleQuery createQuery(TaskRequest request);

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
}
