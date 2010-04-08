package org.simpleds.bg.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.SimpleQuery;
import org.simpleds.bg.TaskRequest;
import org.simpleds.testdb.Dummy1;

public class IterableTaskTest extends AbstractTaskTest {

	private IterableTask<Dummy1> task;
	
	@Before
	public void prepareTest() {
		task = new IterableTask<Dummy1>("set-foo") {

			@Override
			protected SimpleQuery createQuery(TaskRequest request) {
				return entityManager.createQuery(Dummy1.class);
			}

			@Override
			protected void process(Dummy1 entity, TaskRequest request) {
				entity.setName("processed");
				entityManager.put(entity);
			}
		};
		task.withBatchSize(2);
		
		for (int i = 0; i < 3; i++) {
			entityManager.put(Dummy1.create());
		}
	}
	
	@Test
	public void testProceed() {
		assertEntitiesProcessed(0);
		assertEquals(2, task.proceed(request));
		assertEntitiesProcessed(2);
		assertNotNull(request.getCursor());
		assertQueueEntries(1);
		
		// second execution
		assertEquals(1, task.proceed(request));
		assertEntitiesProcessed(3);
	}
	
	private void assertEntitiesProcessed(int count) {
		SimpleQuery query = entityManager.createQuery(Dummy1.class).equal("name", "processed");
		assertEquals(count, entityManager.count(query));
	}
}
