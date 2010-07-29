package org.simpleds.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.simpleds.metadata.ClassMetadata;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

/**
 * A non-caching CacheManager implementation. Using this class will effectively disable caching.
 * @author Nacho
 *
 */
public class NullCacheManagerImpl implements CacheManager {

	@Override
	public void delete(Key key) {
	}

	@Override
	public <T> T get(Key key, ClassMetadata metadata) {
		return null;
	}

	@Override
	public <T> Map<Key, T> get(Collection<Key> keys, ClassMetadata metadata) {
		return null;
	}

	@Override
	public void put(Object instance, Entity entity, ClassMetadata metadata) {
	}

	@Override
	public void delete(Collection<? extends Serializable> keys) {
		
	}

	@Override
	public <T> T get(String key) {
		return null;
	}

	@Override
	public <T> void put(Collection<T> javaObjects, List<Entity> entities, ClassMetadata metadata) {
	}

	@Override
	public void put(String cacheKey, Object value, int seconds) {
	}

}
