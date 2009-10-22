package org.simpleds.test;

import java.io.File;

import org.junit.After;
import org.junit.Before;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.dev.LocalDatastoreService;
import com.google.appengine.tools.development.ApiProxyLocalImpl;
import com.google.apphosting.api.ApiProxy;

public abstract class AbstractDatastoreTest {
	
	protected DatastoreService datastoreService;
	
	/** true to store saved changes, default to false */
	protected boolean storeChanges = false;
	
	@Before
	public void setupDatastore() {
		ApiProxy.setEnvironmentForCurrentThread(new TestEnvironment());
		ApiProxyLocalImpl impl = new ApiProxyLocalImpl(new File("src/test")) { /**/ };
		impl.setProperty(LocalDatastoreService.NO_STORAGE_PROPERTY, Boolean.toString(!storeChanges));
		ApiProxy.setDelegate(impl);
		datastoreService = DatastoreServiceFactory.getDatastoreService();
				
	}
	
	@After
	public void teardownDatastore() {
		ApiProxy.clearEnvironmentForCurrentThread();
		ApiProxy.setDelegate(null);
	}
}
