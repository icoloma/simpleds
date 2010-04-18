package org.simpleds.functions;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.common.base.Function;

/**
 * Converts a Datastore Entity its Key 
 * @author icoloma
 *
 */
public class DatastoreEntityToKeyFunction implements Function<Entity, Key> {
	
	@Override
	public Key apply(Entity entity) {
		return entity.getKey();
	}
	
}