package org.simpleds.tx;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.simpleds.AbstractEntityManagerTest;
import org.simpleds.testdb.Dummy1;

import com.google.appengine.api.datastore.Transaction;

public class TransactionManagerTest extends AbstractEntityManagerTest {

	@Test
	public void testPutCommit() {
		Transaction tx1 = datastoreService.beginTransaction();
		entityManager.put(tx1, Dummy1.create());
		Transaction tx2 = datastoreService.beginTransaction();
		entityManager.put(tx2, Dummy1.create());
		transactionManager.commit();
		assertEquals(2, entityManager.count(entityManager.createQuery(Dummy1.class)));
	}
	
	@Test
	public void testPutRollback() {
		Transaction tx1 = datastoreService.beginTransaction();
		entityManager.put(tx1, Dummy1.create());
		Transaction tx2 = datastoreService.beginTransaction();
		entityManager.put(tx2, Dummy1.create());
		transactionManager.rollback();
		assertEquals(0, entityManager.count(entityManager.createQuery(Dummy1.class)));
	}
	
	@Test
	public void testMultiplePush() {
		Transaction tx1 = datastoreService.beginTransaction();
		entityManager.put(tx1, Dummy1.create());
		transactionManager.commit();
		transactionManager.commit();
		assertEquals(1, entityManager.count(entityManager.createQuery(Dummy1.class)));
	}
	
}
