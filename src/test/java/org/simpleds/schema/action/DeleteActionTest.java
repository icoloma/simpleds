package org.simpleds.schema.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.schema.ActionParamNames;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.repackaged.com.google.common.collect.Maps;

public class DeleteActionTest extends AbstractActionTest {

	private DeleteAction action;
	
	@Before
	public void prepareTest() {
		action = new DeleteAction("delete-test") {
			
			@Override
			protected Query createQuery(Map<String, String> params) {
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
		action.withBatchSize(2);
		assertEntitiesCount(4);
		Map<String, String> params = Maps.newHashMap();
		assertEquals(2, action.proceed("/mock-uri", params));
		assertEntitiesCount(2);
		
		// first execution, should delay work
		params = parseTaskBody();
		assertEquals("delete-test", params.get(ActionParamNames.ACTION));
		assertNotNull(params.get(ActionParamNames.CURSOR));

		// second execution,  finish the work but maybe there is more
		assertEquals(2, action.proceed("/mock-uri", params));
		assertEntitiesCount(0);
		
		// third execution, empty
		params = parseTaskBody();
		assertEquals(0, action.proceed("/mock-uri", params));
        assertQueueEmpty();
	}
	
	@Test
	public void testBigBatch() {
		assertEntitiesCount(4);
		Map<String, String> params = Maps.newHashMap();
		assertEquals(4, action.proceed("/mock-uri", params));
		assertEntitiesCount(0);
		assertQueueEmpty();
	}

	private void assertEntitiesCount(int count) {
		assertEquals(count, datastoreService.prepare(new Query("foo")).countEntities());
	}
	
}
