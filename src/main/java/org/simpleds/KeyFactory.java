package org.simpleds;


/**
 * Creates a KeyFactory.Builder instance
 * @author icoloma
 */
public class KeyFactory {

	/**
	 * Create a Key with the specified parent 
	 * /
	public Key create(Key parentKey) {
		return new com.google.appengine.api.datastore.KeyFactory.Builder(parentKey).getKey();
	}
	
	/**
	 * Create a Key with the specified parent and child value 
	 * /
	public Key create(Key parentKey) {
		return new com.google.appengine.api.datastore.KeyFactory.Builder(parentKey).getKey();
	}*/
	
}
