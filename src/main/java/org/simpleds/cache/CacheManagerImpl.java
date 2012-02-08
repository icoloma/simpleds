package org.simpleds.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.simpleds.functions.DatastoreEntityToKeyFunction;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.collect.Lists;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Implementation of {@link CacheManager}
 * @author icoloma
 */
@Singleton
public class CacheManagerImpl implements CacheManager {

	/** the underlying memcache service */
	private MemcacheService memcache;
	
	@Inject
	private PersistenceMetadataRepository persistenceMetadataRepository;
	
	private static Logger log = LoggerFactory.getLogger(CacheManagerImpl.class);

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
		this.delete(ImmutableList.of(key));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void delete(Collection<? extends Serializable> keys) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
		if (level1 != null) {
			level1.delete(keys);
		}
		memcache.deleteAll((Collection) keys);
		if (log.isDebugEnabled()) {
			log.debug("Deleted from Level 2 cache: " + keys);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<Key, Object> get(Multimap<ClassMetadata, Key> keys) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
		Map<Key, Object> result;
		
		// keys to retrieve from level 1 cache
		if (level1 != null ) {
			List<Key> level1Keys = Lists.newArrayList();
			for (ClassMetadata metadata : keys.keySet()) {
				if (metadata.isCacheable()) {
					level1Keys.addAll(keys.get(metadata));
				}
			}
			result = (Map) level1.get(level1Keys);
		} else {
			result = Maps.newHashMapWithExpectedSize(keys.size());
		}
		
		// keys to retrieve from level 2 cache
		Set<Key> level2Keys = Sets.newHashSet();
		for (ClassMetadata metadata : keys.keySet()) {
			if (metadata.useLevel2Cache()) {
				Collection keysNotFoundInLevel1 = Collections2.filter(keys.get(metadata), new NonCachedPredicate(result.keySet()));
				level2Keys.addAll(keysNotFoundInLevel1);
			}
		}
		
		// retrieve from memcache
		Map<Key, Entity> memcacheResults = level2Keys.isEmpty()? (Map) ImmutableMap.of() : memcache.getAll(level2Keys);
		if (log.isDebugEnabled() && !memcacheResults.isEmpty()) {
			log.debug("Level 2 cache multiple hit: " + memcacheResults.keySet());
		}
		
		// convert from entities to java
		for (Map.Entry<Key, Entity> entry : memcacheResults.entrySet()) {
			Key key = entry.getKey();
			Entity entity = entry.getValue();
			ClassMetadata metadata = persistenceMetadataRepository.get(entity.getKind());
			Object javaObject = metadata.datastoreToJava(entity);
			if (level1 != null) {
				level1.put(entity.getKey(), javaObject);
			}
			result.put(key, javaObject);
		}
		
		return result;
	}

	@Override
	public void put(ListMultimap<ClassMetadata, Object> javaObjects, ListMultimap<ClassMetadata, Entity> dsEntities) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
		Map<Expiration, Map<Key, Entity>> memcacheContents = Maps.newHashMap();
		for (ClassMetadata metadata : javaObjects.keySet()) {
			if (level1 != null && metadata.isCacheable()) {
				level1.put(Collections2.transform(dsEntities.get(metadata), new DatastoreEntityToKeyFunction()), javaObjects.get(metadata));
			}
			if (metadata.useLevel2Cache()) {
				Expiration expiration = metadata.createCacheExpiration();
				Map<Key, Entity> contents = memcacheContents.get(expiration);
				if (contents == null) {
					contents = Maps.newHashMap();
					memcacheContents.put(expiration, contents);
				}
				for (Entity entity : dsEntities.values()) {
					contents.put(entity.getKey(), entity);
				}
			}
		}
		
		// Expiration instances are not bound to an instant in time. If we talk about "2 seconds in the future"
		// it starts counting from the memcache invocation. So, it is legal to use expiration instances as 
		// map keys
		if (!memcacheContents.isEmpty()) {
			for (Map.Entry<Expiration, Map<Key, Entity>> entry : memcacheContents.entrySet()) {
				memcache.putAll(entry.getValue(), entry.getKey());
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
		T result;
		if (level1 != null) {
			result = (T) level1.get(key);
			if (result == null) {
				result = (T) memcache.get(key);
				if (result != null) {
					if (log.isDebugEnabled()) {
						log.debug("Level 2 cache hit: " + key);
					}
					level1.put(key, result);
				}
			}
		} else {
			result = (T) memcache.get(key);
			if (log.isDebugEnabled() && result != null) {
				log.debug("Level 2 cache hit: " + key);
			}
		}
		return result;
	}

	@Override
	public void put(String key, Object value, int seconds) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
		if (level1 != null) {
			level1.put(key, value);
		}
		if (seconds > 0) {
			memcache.put(key, value, Expiration.byDeltaSeconds(seconds));
		}
	}

	public void setMemcache(MemcacheService memcache) {
		this.memcache = memcache;
	}

	public void setPersistenceMetadataRepository(
			PersistenceMetadataRepository persistenceMetadataRepository) {
		this.persistenceMetadataRepository = persistenceMetadataRepository;
	}

}
