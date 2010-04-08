package org.simpleds.bg.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.bg.TaskRequest;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

public class DeleteTaskTest extends AbstractTaskTest {

	private DeleteTask task;
	
	@Before
	public void prepareTest() {
		task = new DeleteTask("delete-test") {
			
			@Override
			protected Query createQuery(TaskRequest request) {
				return new Query("foo");
			}
			
		};
		
		for (int i = 0; i < 4; i++) {
			Entity entity = new Entity("foo");
			datastoreService.put(entity);
		}
	}
	
	@Test
	public void testProceed() {
		// first execution, should delay work
		task.withBatchSize(2);
		assertEntitiesCount(4);
		assertEquals(2, task.proceed(request));
		assertEntitiesCount(2);
		assertQueueEntries(1);
		
		assertNotNull(request.getCursor());

		// second execution,  finish the work but maybe there is more
		assertEquals(2, task.proceed(request));
		assertEntitiesCount(0);
		assertQueueEntries(2);
		
		// third execution, empty
		assertEquals(0, task.proceed(request));
		assertQueueEntries(2);
	}
	
	@Test
	public void testBigBatch() {
		assertEntitiesCount(4);
		assertEquals(4, task.proceed(request));
		assertEntitiesCount(0);
		assertQueueEntries(0);
	}

	private void assertEntitiesCount(int count) {
		assertEquals(count, datastoreService.prepare(new Query("foo")).countEntities());
	}
	
}
