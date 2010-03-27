package org.simpleds.schema.task;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.schema.task.IterableTask;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.repackaged.com.google.common.collect.Maps;

public class IterableTaskTest extends AbstractTaskTest {

	private IterableTask action;
	
	@Before
	public void prepareTest() {
		action = new IterableTask("set-foo") {
			
			@Override
			protected void update(Entity entity) {
				entity.setProperty("foo", Boolean.TRUE);
				datastoreService.put(entity);
			}
			
			@Override
			protected Query createQuery(Map<String, String> params) {
				return new Query("myclass");
			}
		};
		action.withBatchSize(2);
		
		for (int i = 0; i < 3; i++) {
			Entity entity = new Entity("myclass");
			datastoreService.put(entity);
		}
	}
	
	@Test
	public void testProceed() {
		assertEntitiesProcessed(0);
		Map<String, String> params = Maps.newHashMap();
		assertEquals(2, action.proceed("/mock-uri", params));
		assertEntitiesProcessed(2);
		
		// first execution, should delay work
		params = parseTaskBody();
		assertEquals(1, action.proceed("/mock-uri", params));
		assertEntitiesProcessed(3);
        assertQueueEmpty();
	}
	
	private void assertEntitiesProcessed(int count) {
		assertEquals(count, datastoreService.prepare(new Query("myclass").addFilter("foo", FilterOperator.EQUAL, Boolean.TRUE)).countEntities());
	}
}
