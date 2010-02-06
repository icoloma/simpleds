package org.simpleds;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

public abstract class AbstractDatastoreServiceAwareFactory {

	protected DatastoreService datastoreService;

	protected void initDatastoreService() {
		if (datastoreService == null) {
			datastoreService = DatastoreServiceFactory.getDatastoreService();
		}
	}
	
	public void setDatastoreService(DatastoreService datastoreService) {
		this.datastoreService = datastoreService;
	}

}
