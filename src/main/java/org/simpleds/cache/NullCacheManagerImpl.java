package org.simpleds.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

/**
 * A non-caching CacheManager implementation. Injecting this class will effectively disable caching.
 * @author Nacho
 *
 */
public class NullCacheManagerImpl implements CacheManager {

	@Override
	public void put(Object instance, Entity entity) {
	}

	@Override
	public void delete(Key key) {
	}

	@Override
	public void delete(Collection<Key> keys) {
	}

	@Override
	public <T> T get(Key key) {
		return null;
	}

	@Override
	public <T> Map<Key, T> get(Collection<Key> keys) {
		return null;
	}

	@Override
	public void put(Collection javaObjects, List<Entity> entities) {
	}

}
