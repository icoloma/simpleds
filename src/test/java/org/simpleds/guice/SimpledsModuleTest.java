package org.simpleds.guice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.simpleds.EntityManager;
import org.simpleds.EntityManagerFactory;
import org.simpleds.testdb.CacheableEntity;
import org.simpleds.testdb.Dummy1;
import org.simpleds.tx.TransactionalService;
import org.simpleds.tx.TransactionalServiceImpl;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;

public class SimpledsModuleTest {

	@Test
	public void testModule() {
		new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()).setUp();
		Injector injector = Guice.createInjector(
			new SimpledsModule()
				.withPersistentClasses(Dummy1.class, CacheableEntity.class)
				.withTransactionsFor(Matchers.subclassesOf(TransactionalServiceImpl.class))
				.withJRebel()
			,
			new TestModule()
		);
		
		// entity manager
		EntityManager entityManager = injector.getInstance(EntityManager.class);
		assertNotNull(entityManager);
		assertSame(entityManager, EntityManagerFactory.getEntityManager());
		entityManager.put(CacheableEntity.create());
		
		// transaction fail
		TransactionalService transactionalService = injector.getInstance(TransactionalService.class);
		try {
			transactionalService.saveFailure();
			fail("Should have failed!");
		} catch (Exception e) {
			assertTrue(e.toString(), e instanceof SecurityException);
			assertTrue(entityManager.getDatastoreService().getActiveTransactions().isEmpty());
			assertEquals(0, entityManager.createQuery(Dummy1.class).count());
		}
		
		// transaction success
		transactionalService.saveSuccess();
		assertEquals(2, entityManager.createQuery(Dummy1.class).count());
	}
	
	private class TestModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(TransactionalService.class).to(TransactionalServiceImpl.class);
		}
		
	}
	
}
