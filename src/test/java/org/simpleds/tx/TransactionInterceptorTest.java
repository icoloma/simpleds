package org.simpleds.tx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simpleds.EntityManager;
import org.simpleds.test.AbstractDatastoreTest;
import org.simpleds.testdb.Dummy1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.appengine.api.datastore.Key;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:org/simpleds/tx/tx-test.xml" })
public class TransactionInterceptorTest extends AbstractDatastoreTest {

	@Autowired
	private TransactionalService transactionalService;
	
	@Autowired
	private EntityManager entityManager;
	
	@Test
	public void testCommit() throws Exception {
		assertTrue(transactionalService.getClass().getName().startsWith("$Proxy"));
		assertCommit("saveSuccess");
		assertCommit("saveWithException");
		assertCommit("saveWithException2");
	}
	
	@Test
	public void testRollback() throws Exception {
		assertRollback("saveFailure");
		assertRollback("saveWithException");
		assertRollback("saveWithException2");
	}

	private void assertCommit(String methodName) throws Exception {
		try {
			clean();
			getMethod(methodName).invoke(transactionalService, false);
		} catch (InvocationTargetException e) {
			// expected noRollback exception
			assertTrue(e.getTargetException() instanceof UnsupportedOperationException);
		}
		assertEquals(2, entityManager.count(entityManager.createQuery(Dummy1.class)));
	}
	
	private void assertRollback(String methodName) throws Exception {
		try {
			clean();
			getMethod(methodName).invoke(transactionalService, true);
			fail("No exception was thrown from method");
		} catch (InvocationTargetException e) {
			// expected rollback exception
			assertTrue(e.getTargetException() instanceof SecurityException);
		}
		assertEquals(0, entityManager.count(entityManager.createQuery(Dummy1.class)));
	}

	public void clean() {
		List<Key> keys = entityManager.find(entityManager.createQuery(Dummy1.class).keysOnly());
		entityManager.delete(keys);
	}

	private Method getMethod(String methodName) throws Exception {
		return transactionalService.getClass().getDeclaredMethod(methodName, new Class[] { Boolean.TYPE });
	}

}