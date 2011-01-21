package org.simpleds.functions;

import org.simpleds.EntityManager;
import org.simpleds.EntityManagerFactory;
import org.simpleds.metadata.PropertyMetadata;

import com.google.appengine.api.datastore.Key;
import com.google.common.base.Function;

/**
 * Converts a persistent instance into its Key. This function works with collections containing items of different types.  
 * @author icoloma
 *
 */
public class HeterogeneousEntityToKeyFunction implements Function<Object, Key> {
	
	private EntityManager entityManager = EntityManagerFactory.getEntityManager();
	
	@Override
	public Key apply(Object instance) {
		if (instance == null) {
			return null;
		}
		PropertyMetadata<Key, Key> propertyMetadata = (PropertyMetadata<Key, Key>) entityManager.getClassMetadata(instance.getClass());
		return propertyMetadata.getValue(instance);
	}
	
}