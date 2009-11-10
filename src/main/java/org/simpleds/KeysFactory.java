package org.simpleds;

import java.util.Collection;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.collect.Lists;

/**
 * Creates collections of keys 
 * @author Nacho
 */
public class KeysFactory {

	/**
	 * Transform a collection of id values to the equivalent list of {@link Key}
	 * @param parentKey the parent key, if any. Can be null.
	 * @param kind the kind of keys to produce.
	 * @param ids the long values to use as keys
	 */
	public static Collection<Key> create(Key parentKey, String kind, Collection<Long> ids) {
		List<Key> keys = Lists.newArrayListWithCapacity(ids.size());
		for (Long id : ids) {
			keys.add(KeyFactory.createKey(parentKey, kind, id));
		}
		return keys;
	}
	
	public static Collection<Key> create(String kind, Collection<Long> ids) {
		return create(null, kind, ids);
	}
	
	public static Collection<Key> create(Key parentKey, Class<?> clazz, Collection<Long> ids) {
		return create(parentKey, clazz.getSimpleName(), ids);
	}
	
	public static Collection<Key> create(Class<?> clazz, Collection<Long> ids) {
		return create(clazz.getSimpleName(), ids);
	}
	
}
