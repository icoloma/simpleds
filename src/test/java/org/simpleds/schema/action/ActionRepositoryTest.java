package org.simpleds.schema.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.schema.ActionRepository;
import org.simpleds.schema.ActionParamNames;
import org.springframework.mock.web.MockHttpServletRequest;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

public class ActionRepositoryTest extends AbstractActionTest {

	private ActionRepository repository;
	
	private MockHttpServletRequest request;
	
	@Before
	public void initRepository() {
		repository = new ActionRepository().add(
			new DeleteSessionsAction().withBatchSize(2).add(new ClearCacheAction())
		);
		long farFuture = System.currentTimeMillis() + 10 * 24L * 60 * 60 * 1000;
		// sessions that should not be removed
		createSession(farFuture);
		createSession(farFuture);
		
		// sessions that should be removed
		createSession(0);
		createSession(1);
		createSession(2);
		request = new MockHttpServletRequest("GET", "/mock-uri");
	}
	
	/**
	 * Mocks the behavior in AppEngine
	 * @param expires
	 */
	private void createSession(long expires) {
		Entity session = new Entity("_ah_SESSION");
		session.setProperty("_expires", expires);
		datastoreService.put(session);
		memcache.put(session.getKey(), session);
	}

	@Test
	public void testProceed() throws Exception {
		assertEquals(5, datastoreService.prepare(new Query("_ah_SESSION")).countEntities());
		
		// first execution, just defer
		repository.proceed(request);
		assertActionAndCursor("delete-sessions", false);
		
		// second execution, start deleting sessions
		repository.proceed(request);
		assertActionAndCursor("delete-sessions", true);
		
		// third execution, finish deleting sessions and defer clear-cache
		repository.proceed(request);
		assertActionAndCursor("delete-sessions/clear-cache", false);
		
		// fourth execution, clear cache
		repository.proceed(request);
		assertQueueEmpty();
		assertEquals(2, datastoreService.prepare(new Query("_ah_SESSION")).countEntities());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalActionName() throws Exception {
		request.setParameter(ActionParamNames.ACTION, "xxx");
		repository.proceed(request);
	}

	private void assertActionAndCursor(String action, boolean cursor) throws Exception {
		Map<String, String> next = reset();
		assertEquals(action, next.get(ActionParamNames.ACTION));
		if (cursor) {
			assertNotNull(next.get(ActionParamNames.CURSOR));
		} else {
			assertNull(next.get(ActionParamNames.CURSOR));
		}
	}
	
	
	protected Map<String, String> reset() throws UnsupportedEncodingException {
		Map<String, String> next = parseTaskBody();
		request.removeAllParameters();
		for (Map.Entry<String, String> entry : next.entrySet()) {
			request.setParameter(entry.getKey(), entry.getValue());
		}
		return next;
	}
	
}
