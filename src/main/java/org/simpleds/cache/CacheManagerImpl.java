package org.simpleds.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PersistenceMetadataRepository;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;

public class CacheManagerImpl implements CacheManager {

	/** the underlying memcache service */
	private MemcacheService memcache;
	
	/** the {@link PersistenceMetadataRepository} instance */
	private PersistenceMetadataRepository persistenceMetadataRepository;
	
	@Override
	public <T> T get(Key key) {
		Level1Cache level1 = Level1Cache.get();
		T cachedValue = null;
		if (level1 != null) {
			cachedValue = level1.get(key);
		}
		if (cachedValue == null) {
			Entity entity = (Entity) memcache.get(key);
			if (entity != null) {
				ClassMetadata metadata = persistenceMetadataRepository.get(key.getKind());
				cachedValue = metadata.datastoreToJava(entity);
				if (level1 != null) {
					level1.put(cachedValue, entity);
				}
			}
		}
		return cachedValue;
	}

	@Override
	public void put(Object instance, Entity entity) {
		Level1Cache level1 = Level1Cache.get();
		if (level1 != null) {
			level1.put(instance, entity);
		}
		memcache.put(entity.getKey(), entity);
	}

	@Override
	public void delete(Key key) {
		Level1Cache level1 = Level1Cache.get();
		if (level1 != null) {
			level1.delete(key);
		}
		memcache.delete(key);
	}

	@Override
	public void delete(Collection<Key> keys) {
		Level1Cache level1 = Level1Cache.get();
		if (level1 != null) {
			level1.delete(keys);
		}
		memcache.deleteAll((Collection) keys);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Map<Key, T> get(Collection<Key> keys) {
		Level1Cache level1 = Level1Cache.get();
		Map<Key, T> result = null;
		if (level1 != null) {
			result = level1.get(keys);
		}
		Map result2 = memcache.getAll(result == null? (Collection) keys : (Collection) Collections2.filter(keys, new NonCachedPredicate(result.keySet())));
		if (result == null) {
			result = Maps.newHashMapWithExpectedSize(keys.size());
		}
		Set<Map.Entry> entrySet = result2.entrySet();
		for (Map.Entry entry : entrySet) {
			Key key = (Key) entry.getKey();
			Entity entity = (Entity) entry.getValue();
			ClassMetadata metadata = persistenceMetadataRepository.get(key.getKind());
			Object javaObject = metadata.datastoreToJava(entity);
			if (level1 != null) {
				level1.put(javaObject, entity);
			}
			result.put(key, (T) javaObject);
		}
		return result;
	}

	@Override
	public void put(Collection javaObjects, List<Entity> entities) {
		Level1Cache level1 = Level1Cache.get();
		if (level1 != null) {
			level1.put(javaObjects, entities);
		}
		Map<Object, Object> map = Maps.newHashMap();
		for (Entity entity : entities) {
			map.put(entity.getKey(), entity);
		}
		memcache.putAll(map);
	}

	public void setMemcache(MemcacheService memcache) {
		this.memcache = memcache;
	}

	public void setPersistenceMetadataRepository(
			PersistenceMetadataRepository persistenceMetadataRepository) {
		this.persistenceMetadataRepository = persistenceMetadataRepository;
	}

}
