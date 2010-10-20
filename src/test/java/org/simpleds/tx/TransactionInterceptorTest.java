package org.simpleds.tx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simpleds.EntityManager;
import org.simpleds.test.AbstractDatastoreTest;
import org.simpleds.testdb.Dummy1;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.appengine.api.datastore.Key;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:org/simpleds/tx/tx-test.xml" })
public class TransactionInterceptorTest extends AbstractDatastoreTest {

	@Inject
	private TransactionalService transactionalService;
	
	@Inject
	private EntityManager entityManager;
	
	@After
	public void assertAllClosed() {
		assertTrue(datastoreService.getActiveTransactions().isEmpty());
	}
	
	@Test
	public void testCommit() throws Exception {
		assertTrue(transactionalService.getClass().getName().startsWith("$Proxy"));
		assertCommit("saveSuccess");
	}
	
	@Test
	public void testRollback() throws Exception {
		assertRollback("saveFailure");
	}

	private void assertCommit(String methodName) throws Exception {
		try {
			clean();
			getMethod(methodName).invoke(transactionalService);
		} catch (InvocationTargetException e) {
			// expected noRollback exception
			Throwable exc = e.getTargetException();
			if(!(exc instanceof UnsupportedOperationException)) {
				exc.printStackTrace();
				fail(exc.toString());
			}
		}
		assertEquals(2, entityManager.createQuery(Dummy1.class).count());
	}
	
	private void assertRollback(String methodName) throws Exception {
		try {
			clean();
			getMethod(methodName).invoke(transactionalService);
			fail("No exception was thrown from method");
		} catch (InvocationTargetException e) {
			// expected rollback exception
			assertTrue(e.getTargetException() instanceof SecurityException);
		}
		assertEquals(0, entityManager.createQuery(Dummy1.class).count());
	}

	public void clean() {
		List<Key> keys = entityManager.createQuery(Dummy1.class).keysOnly().asList();
		entityManager.delete(keys);
	}

	private Method getMethod(String methodName) throws Exception {
		return transactionalService.getClass().getDeclaredMethod(methodName);
	}

}
