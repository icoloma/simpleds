package org.simpleds;

import java.util.Collection;

import org.simpleds.functions.IdToKeyFunction;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.collect.Collections2;

/**
 * Extra methods not provided by KeyFactory class
 * @author Nacho
 */
public class KeyFactory2 {

	/**
	 * Creates a key using the class simple name as kind
	 */
	public static Key createKey(Class<?> clazz, long id) {
		return KeyFactory.createKey(clazz.getSimpleName(), id);
	}
	
	/**
	 * Creates a key using the class simple name as kind
	 */
	public static Key createKey(Class<?> clazz, String name) {
		return KeyFactory.createKey(clazz.getSimpleName(), name);
	}
	
	/**
	 * Creates a key using the class simple name as kind
	 */
	public static Key createKey(Key parent, Class<?> clazz, long id) {
		return KeyFactory.createKey(parent, clazz.getSimpleName(), id);
	}
	
	/**
	 * Creates a key using the class simple name as kind
	 */
	public static Key createKey(Key parent, Class<?> clazz, String name) {
		return KeyFactory.createKey(parent, clazz.getSimpleName(), name);
	}
	
	/**
	 * Transform a collection of id values to the equivalent list of {@link Key}
	 * @param parentKey the parent key, if any. Can be null.
	 * @param kind the kind of keys to produce.
	 * @param ids the long values to use as keys
	 * @deprecated use Collections2.transform(ids, new IdToKeyFunction(kind).withParent(parentKey));
	 */
	@Deprecated
	public static Collection<Key> create(Key parentKey, String kind, Collection<Long> ids) {
		return Collections2.transform(ids, new IdToKeyFunction(kind).withParent(parentKey));
	}

	/**
	 * @deprecated use Collections2.transform(ids, new IdToKeyFunction(kind));
	 */
	@Deprecated
	public static Collection<Key> create(String kind, Collection<Long> ids) {
		return Collections2.transform(ids, new IdToKeyFunction(kind));
	}
	
	/**
	 * @deprecated use Collections2.transform(ids, new IdToKeyFunction(clazz).withParent(parentKey));
	 */
	@Deprecated
	public static Collection<Key> create(Key parentKey, Class<?> clazz, Collection<Long> ids) {
		return Collections2.transform(ids, new IdToKeyFunction(clazz).withParent(parentKey));
	}
	
	/**
	 * @deprecated use Collections2.transform(ids, new IdToKeyFunction(clazz));
	 */
	@Deprecated
	public static Collection<Key> create(Class<?> clazz, Collection<Long> ids) {
		return Collections2.transform(ids, new IdToKeyFunction(clazz));
	}
	
}
