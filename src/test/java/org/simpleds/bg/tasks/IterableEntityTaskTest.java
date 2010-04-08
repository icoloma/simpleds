package org.simpleds.bg.tasks;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.bg.TaskRequest;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

public class IterableEntityTaskTest extends AbstractTaskTest {

	private IterableEntityTask task;
	
	@Before
	public void prepareTest() {
		task = new IterableEntityTask("set-foo") {
			
			@Override
			protected void process(Entity entity, TaskRequest request) {
				entity.setProperty("foo", Boolean.TRUE);
				datastoreService.put(entity);
			}
			
			@Override
			protected Query createQuery(TaskRequest request) {
				return new Query("myclass");
			}
		};
		task.withBatchSize(2);
		
		for (int i = 0; i < 3; i++) {
			Entity entity = new Entity("myclass");
			datastoreService.put(entity);
		}
	}
	
	@Test
	public void testProceed() {
		assertEntitiesProcessed(0);
		assertEquals(2, task.proceed(request));
		assertEntitiesProcessed(2);
		assertQueueEntries(1);
		
		// first execution, should delay work
		assertEquals(1, task.proceed(request));
		assertEntitiesProcessed(3);
        assertQueueEntries(1);
	}
	
	private void assertEntitiesProcessed(int count) {
		assertEquals(count, datastoreService.prepare(new Query("myclass").addFilter("foo", FilterOperator.EQUAL, Boolean.TRUE)).countEntities());
	}
}
