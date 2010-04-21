package org.simpleds.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.simpleds.functions.DatastoreEntityToKeyFunction;
import org.simpleds.metadata.ClassMetadata;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;

public class CacheManagerImpl implements CacheManager {

	/** the underlying memcache service */
	private MemcacheService memcache;
	
	private Log log = LogFactory.getLog(CacheManagerImpl.class);

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Key key, ClassMetadata metadata) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
		T cachedValue = null;
		if (level1 != null) {
			cachedValue = (T) level1.get(key);
		}
		if (cachedValue == null && metadata.useLevel2Cache()) {
			Entity entity = (Entity) memcache.get(key);
			if (entity != null) {
				if (log.isDebugEnabled()) {
					log.debug("Level 2 cache hit: " + key);
				}
				cachedValue = (T) metadata.datastoreToJava(entity);
				if (level1 != null) {
					level1.put(entity.getKey(), cachedValue);
				}
			}
		}
		return cachedValue;
	}

	@Override
	public void put(Object instance, Entity entity, ClassMetadata metadata) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
		if (level1 != null) {
			level1.put(entity.getKey(), instance);
		}
		if (metadata.useLevel2Cache()) {
			memcache.put(entity.getKey(), entity, metadata.createCacheExpiration());
		}
	}

	@Override
	public void delete(Key key) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
		if (level1 != null) {
			level1.delete(key);
		}
		memcache.delete(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void delete(Collection<Key> keys) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
		if (level1 != null) {
			level1.delete(keys);
		}
		memcache.deleteAll((Collection) keys);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Map<Key, T> get(Collection<Key> keys, ClassMetadata metadata) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
		Map<Key, T> result = null;
		if (level1 != null) {
			result = level1.get(keys);
		}
		if (!metadata.useLevel2Cache()) {
			return result;
		}
		Map result2 = memcache.getAll(result == null? (Collection) keys : (Collection) Collections2.filter(keys, new NonCachedPredicate(result.keySet())));
		if (result == null) {
			result = Maps.newHashMapWithExpectedSize(keys.size());
		}
		if (log.isDebugEnabled() && !result2.isEmpty()) {
			log.debug("Level 2 cache multiple hit: " + result2.keySet());
		}
		for (Map.Entry entry : (Set<Map.Entry>) result2.entrySet()) {
			Key key = (Key) entry.getKey();
			Entity entity = (Entity) entry.getValue();
			Object javaObject = metadata.datastoreToJava(entity);
			if (level1 != null) {
				level1.put(entity.getKey(), javaObject);
			}
			result.put(key, (T) javaObject);
		}
		return result;
	}

	@Override
	public <T> void put(Collection<T> javaObjects, List<Entity> entities, ClassMetadata metadata) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
		if (level1 != null) {
			level1.put(Collections2.transform(entities, new DatastoreEntityToKeyFunction()), javaObjects);
		}
		if (metadata.useLevel2Cache()) {
			Map<Object, Object> map = Maps.newHashMap();
			for (Entity entity : entities) {
				map.put(entity.getKey(), entity);
			}
			memcache.putAll(map, metadata.createCacheExpiration());
		}
	}

	public void setMemcache(MemcacheService memcache) {
		this.memcache = memcache;
	}

}
