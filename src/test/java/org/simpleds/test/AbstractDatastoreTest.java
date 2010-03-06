package org.simpleds.test;

import org.junit.After;
import org.junit.Before;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public abstract class AbstractDatastoreTest {

	protected LocalServiceTestHelper helper =
		new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig().setBackingStoreLocation("build").setNoStorage(true));

	protected DatastoreService datastoreService;
	
	/** true to store saved changes, default to false */
	protected boolean storeChanges = false;
	
	@Before
	public void setupDatastore() {
		helper.setUp();
		datastoreService = DatastoreServiceFactory.getDatastoreService();
				
	}
	
	@After
	public void teardownDatastore() {
		helper.tearDown();
	}
	
}
