package org.simpleds.tx;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.simpleds.AbstractEntityManagerTest;
import org.simpleds.testdb.Dummy1;

import com.google.appengine.api.datastore.Transaction;

public class TransactionManagerTest extends AbstractEntityManagerTest {

	
	@Test
	public void testPutCommit() {
		Transaction tx1 = entityManager.beginTransaction();
		entityManager.put(tx1, Dummy1.create());
		Transaction tx2 = entityManager.beginTransaction();
		entityManager.put(tx2, Dummy1.create());
		entityManager.commit();
		assertEquals(2, entityManager.count(entityManager.createQuery(Dummy1.class)));
	}
	
	@Test
	public void testPutRollback() {
		Transaction tx1 = entityManager.beginTransaction();
		entityManager.put(tx1, Dummy1.create());
		Transaction tx2 = entityManager.beginTransaction();
		entityManager.put(tx2, Dummy1.create());
		entityManager.rollback();
		assertEquals(0, entityManager.count(entityManager.createQuery(Dummy1.class)));
	}
	
}
