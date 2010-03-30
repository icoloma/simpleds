package org.simpleds;

import java.util.Collection;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.collect.Lists;

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
	 */
	public static List<Key> create(Key parentKey, String kind, Collection<Long> ids) {
		List<Key> keys = Lists.newArrayListWithCapacity(ids.size());
		for (Long id : ids) {
			keys.add(KeyFactory.createKey(parentKey, kind, id));
		}
		return keys;
	}
	
	public static List<Key> create(String kind, Collection<Long> ids) {
		return create(null, kind, ids);
	}
	
	public static List<Key> create(Key parentKey, Class<?> clazz, Collection<Long> ids) {
		return create(parentKey, clazz.getSimpleName(), ids);
	}
	
	public static List<Key> create(Class<?> clazz, Collection<Long> ids) {
		return create(clazz.getSimpleName(), ids);
	}
	
}
