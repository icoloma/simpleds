package org.simpleds.functions;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * Transforms a String name into a Key 
 * @author icoloma
 *
 */
public class NameToKeyFunction extends AbstractPartialKeyFunction<String> {

	public NameToKeyFunction(String kind) {
		super(kind);
	}

	public NameToKeyFunction(Class<?> persistentClass) {
		super(persistentClass);
	}

	@Override
	public Key apply(String name) {
		return KeyFactory.createKey(parentKey, kind, name);
	}

	@Override
	public NameToKeyFunction withParent(Key parentKey) {
		return (NameToKeyFunction) super.withParent(parentKey);
	}
	
}
