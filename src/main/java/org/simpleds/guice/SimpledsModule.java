package org.simpleds.guice;

import java.util.Set;

import org.simpleds.EntityManager;
import org.simpleds.EntityManagerImpl;
import org.simpleds.annotations.Transactional;
import org.simpleds.cache.CacheManager;
import org.simpleds.cache.CacheManagerImpl;
import org.simpleds.metadata.ClassMetadataReloader;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.tx.TransactionManager;
import org.simpleds.tx.TransactionManagerImpl;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;

public class SimpledsModule extends AbstractModule {

	/** the collection of persistent classes to register */
	private Set<Class<?>> persistentClasses;
	
	/** the class matcher to apply transactional interceptor */
	private Matcher<? super Class<?>> transactionClassMatcher;
	
	/** enable JRebel reloading */
	private boolean jrebel;
	
	@Override
	protected void configure() {
		initDatastoreService();
		initPersistenceMetadataRepository();
		initCacheManager();
		initTransactionManager();
		initEntityManager();
		initJRebel();
	}

	public SimpledsModule withPersistentClasses(Class<?>... persistentClasses) {
		this.persistentClasses = Sets.newHashSet(persistentClasses);
		return this;
	}
	
	/**
	 * Configure transparent transaction interceptors for methods annotated as {@link Transactional}
	 * @param matcher the matcher that selects the classes to consider for transaction interceptors
	 * @return this same instance
	 */
	public SimpledsModule withTransactionsFor(Matcher<? super Class<?>> matcher) {
		this.transactionClassMatcher = matcher;
		return this;
	}
	
	/**
	 * Register listeners to enable class reloading using JRebel
	 */
	public SimpledsModule withJRebel() {
		jrebel = true;
		return this;
	}

	protected void initDatastoreService() {
		bind(DatastoreService.class).toInstance(DatastoreServiceFactory.getDatastoreService());
	}
	
	protected void initPersistenceMetadataRepository() {
		if (persistentClasses == null) {
			throw new IllegalArgumentException("No persistent class has been specified");
		}
		bind(new TypeLiteral<Set<Class<?>>>() {}).annotatedWith(PersistentClasses.class).toInstance(persistentClasses);
		bind(PersistenceMetadataRepository.class).to(GuicePersistenceMetadataRepository.class);
	}

	protected void initCacheManager() {
		CacheManagerImpl cmi = new CacheManagerImpl();
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService(CacheManager.MEMCACHE_NAMESPACE);
		cmi.setMemcache(memcache);
		bind(CacheManager.class).toInstance(cmi);
	}

	protected void initTransactionManager() {
		bind(TransactionManager.class).to(TransactionManagerImpl.class);
		if (transactionClassMatcher != null) {
			GuiceTransactionInterceptor transactionInterceptor = new GuiceTransactionInterceptor();
			requestInjection(transactionInterceptor);
			bindInterceptor(transactionClassMatcher, Matchers.annotatedWith(Transactional.class), transactionInterceptor);
		}
	}

	protected void initEntityManager() {
		bind(EntityManager.class).to(EntityManagerImpl.class);
	}

	protected void initJRebel() {
		if (jrebel) {
			bind(ClassMetadataReloader.class).asEagerSingleton();
		}
	}

}
