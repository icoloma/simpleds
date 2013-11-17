package org.simpleds.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.SetMultimap;
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
    public <T> T get(ClassMetadata metadata, Key key) {
        return null;  
    }

    @Override
	public void put(Object instance, Entity entity, ClassMetadata metadata) {
	}

    @Override
    public Map<Key, Object> get(SetMultimap<ClassMetadata, Key> keys) {
        return null;  
    }

    @Override
    public <T> T get(String namespace, String key) {
        return null;  
    }

    @Override
	public void put(ListMultimap<ClassMetadata, Object> javaObjects,
			ListMultimap<ClassMetadata, Entity> dsEntities) {
		
	}

    @Override
    public void put(String cacheNamespace, String cacheKey, Object value, int seconds) {
        
    }

    @Override
    public void delete(SetMultimap<String, ? extends Serializable> keys) {
        
    }

    @Override
    public void clear(String namespace) {

    }

}
