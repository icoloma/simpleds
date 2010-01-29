package org.simpleds.tx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.simpleds.AbstractEntityManagerTest;
import org.simpleds.testdb.Dummy1;

import com.google.appengine.api.datastore.Transaction;

public class TransactionManagerTest extends AbstractEntityManagerTest {

	private static Log log = LogFactory.getLog(TransactionManagerTest.class);
	
	@Test
	public void testPutCommit() {
		transactionManager.pushContext();
		Transaction tx1 = transactionManager.beginTransaction();
		entityManager.put(tx1, Dummy1.create());
		Transaction tx2 = transactionManager.beginTransaction();
		entityManager.put(tx2, Dummy1.create());
		transactionManager.commit();
		assertEquals(2, entityManager.count(entityManager.createQuery(Dummy1.class)));
	}
	
	@Test
	public void testPutRollback() {
		transactionManager.pushContext();
		Transaction tx1 = transactionManager.beginTransaction();
		entityManager.put(tx1, Dummy1.create());
		Transaction tx2 = transactionManager.beginTransaction();
		entityManager.put(tx2, Dummy1.create());
		transactionManager.rollback();
		assertEquals(0, entityManager.count(entityManager.createQuery(Dummy1.class)));
	}
	
	@Test
	public void testMultiplePush() {
		transactionManager.pushContext();
		transactionManager.pushContext();
		Transaction tx1 = transactionManager.beginTransaction();
		entityManager.put(tx1, Dummy1.create());
		transactionManager.commit();
		transactionManager.commit();
		assertEquals(1, entityManager.count(entityManager.createQuery(Dummy1.class)));
	}
	
	@Test(expected=IllegalStateException.class)
	public void testPushContextNotInvoked() {
		transactionManager.beginTransaction();
	}
	
	@Test
	public void testUnevenCommit() {
		transactionManager.pushContext();
		transactionManager.beginTransaction();
		transactionManager.commit();
		try {
			transactionManager.commit();
			fail();
		} catch (IllegalStateException e) {
			log.debug(e);
		}
	}
	
}
