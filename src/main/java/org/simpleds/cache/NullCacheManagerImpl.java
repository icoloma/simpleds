package org.simpleds.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.simpleds.metadata.ClassMetadata;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

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
	public void put(String cacheKey, Object value, int seconds) {
	}

	@Override
	public Map<Key, Object> get(Multimap<ClassMetadata, Key> keys) {
		return null;
	}

	@Override
	public void put(ListMultimap<ClassMetadata, Object> javaObjects,
			ListMultimap<ClassMetadata, Entity> dsEntities) {
		
	}

}
