package org.simpleds.functions;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * Transforms a numeric id into a Key 
 * @author icoloma
 *
 */
public class IdToKeyFunction extends AbstractPartialKeyFunction<Long> {

	public IdToKeyFunction(String kind) {
		super(kind);
	}

	public IdToKeyFunction(Class<?> persistentClass) {
		super(persistentClass);
	}

	@Override
	public Key apply(Long id) {
		return KeyFactory.createKey(parentKey, kind, id);
	}

	@Override
	public IdToKeyFunction withParent(Key parentKey) {
		return (IdToKeyFunction) super.withParent(parentKey);
	}
	
}
