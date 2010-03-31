package org.simpleds.functions;

import org.simpleds.EntityManagerFactory;

import com.google.appengine.api.datastore.Key;

/**
 * Converts a persistent instance into a foreign key reference
 * @author icoloma
 *
 */
public class EntityToForeignEntityFunction<T> extends AbstractPropertyFunction<T, Key> {
	
	public EntityToForeignEntityFunction(Class<T> clazz, String propertyName) {
		super(clazz, propertyName);
	}
	
	@Override
	public Key apply(T instance) {
		Key foreignKey = (Key)propertyMetadata.getValue(instance);
		return EntityManagerFactory.getEntityManager().get(foreignKey);
	}
	
}