package org.simpleds.test;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.labs.taskqueue.dev.LocalTaskQueue;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

public abstract class AbstractDatastoreTest {

	protected LocalServiceTestHelper helper =
		new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig().setBackingStoreLocation("build").setNoStorage(true));

	protected DatastoreService datastoreService;
	
	protected LocalTaskQueue queue;
	
	protected MemcacheService memcache;
	
	/** true to store saved changes, default to false */
	protected boolean storeChanges = false;
	
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	public AbstractDatastoreTest() {
		// to let Spring initialize Datastore-dependent classes
		helper.setUp();
		SLF4JBridgeHandler.install();
	}
	
	@Before
	public void setupDatastore() {
		helper.setUp();
		datastoreService = DatastoreServiceFactory.getDatastoreService();
		queue = LocalTaskQueueTestConfig.getLocalTaskQueue();
		memcache = MemcacheServiceFactory.getMemcacheService();
	}
	
	@After
	public void teardownDatastore() {
		helper.tearDown();
	}

}
