package org.simpleds.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import org.simpleds.functions.DatastoreEntityToKeyFunction;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PersistenceMetadataRepository;
import org.simpleds.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;

/**
 * Implementation of {@link CacheManager}
 * @author icoloma
 */
@Singleton
public class CacheManagerImpl implements CacheManager {

	@Inject
	private PersistenceMetadataRepository persistenceMetadataRepository;
	
	private static Logger log = LoggerFactory.getLogger(CacheManagerImpl.class);

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(ClassMetadata metadata, Key key) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
		T cachedValue = null;
		if (level1 != null) {
			cachedValue = (T) level1.get(key);
		}
		if (cachedValue == null && metadata.useLevel2Cache()) {
			Entity entity = (Entity) getMemcache(metadata.getCacheNamespace()).get(key);
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
            getMemcache(metadata.getCacheNamespace()).put(entity.getKey(), entity, Expiration.byDeltaSeconds(metadata.getCacheSeconds()));
		}
	}

    @Override
    public void delete(SetMultimap<String, ? extends Serializable> keysByNamespace) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
		if (level1 != null) {
			level1.delete(keysByNamespace.values());
		}

        for (String namespace : keysByNamespace.keySet()) {
            getMemcache(namespace).deleteAll(keysByNamespace.get(namespace));
        }
		if (log.isDebugEnabled()) {
			log.debug("Deleted from Level 2 cache: " + keysByNamespace.values());
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<Key, Object> get(SetMultimap<ClassMetadata, Key> keys) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
        SetMultimap<String, Key> level2Keys = HashMultimap.create();
        Map<Key, Object> result = Maps.newHashMapWithExpectedSize(keys.size());

		// keys to retrieve from level 1 cache
        for (ClassMetadata metadata : keys.keySet()) {
            if (metadata.isCacheable()) {
                Set<Key> partialKeys = keys.get(metadata);
                if (level1 != null ) {
                    Map<Key, Object> cached1Results = (Map) level1.get(partialKeys);
                    result.putAll(cached1Results);
                    if (metadata.useLevel2Cache()) {
                        level2Keys.putAll(metadata.getCacheNamespace(),  Sets.difference(partialKeys, cached1Results.keySet()));
                    }
                } else {
                    level2Keys.putAll(metadata.getCacheNamespace(),  partialKeys);
                }
            }
        }

        // retrieve from memcache
        for (String namespace : level2Keys.keySet()) {
            Map<Key, Entity> cached2Results = (Map) getMemcache(namespace).getAll(level2Keys.get(namespace));
            for (Map.Entry<Key, Entity> entry : cached2Results.entrySet()) {
                Key key = entry.getKey();
                Entity entity = entry.getValue();
                if (entity != null) {
                    ClassMetadata metadata = persistenceMetadataRepository.get(entity.getKind());
                    Object javaObject = metadata.datastoreToJava(entry.getValue());
                    result.put(entry.getKey(), javaObject);
                    if (level1 != null) {
                        level1.put(entity.getKey(), javaObject);
                    }
                }
            }
            if (log.isDebugEnabled() && !cached2Results.isEmpty()) {
                log.debug("Level 2 cache multiple hit: " + cached2Results.keySet());
            }
        }

		return result;
	}

	@Override
	public void put(ListMultimap<ClassMetadata, Object> javaObjects, ListMultimap<ClassMetadata, Entity> dsEntities) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
        SetMultimap<Pair<String, Integer>, Entity> level2WriteContents = HashMultimap.create(); // the key is Pair<namespace, expirationInSeconds>
		for (ClassMetadata metadata : javaObjects.keySet()) {
			if (level1 != null && metadata.isCacheable()) {
				level1.put(Collections2.transform(dsEntities.get(metadata), new DatastoreEntityToKeyFunction()), javaObjects.get(metadata));
			}
			if (metadata.useLevel2Cache()) {
                Pair<String, Integer> key = new Pair(metadata.getCacheNamespace(), metadata.getCacheSeconds());
                level2WriteContents.putAll(key, dsEntities.get(metadata));
			}
		}

        for (Pair<String, Integer> key : level2WriteContents.keySet()) {
            Set<Entity> contents = level2WriteContents.get(key);
            Map<Key, Entity> m = Maps.newHashMapWithExpectedSize(contents.size());
            for (Entity e : contents) {
                m.put(e.getKey(), e);
            }
            getMemcache(key.getValue1()).putAll(m, Expiration.byDeltaSeconds(key.getValue2()));
        }
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String namespace, String key) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
		T result;
		if (level1 != null) {
			result = (T) level1.get(key);
			if (result == null) {
				result = (T) getMemcache(namespace).get(key);
				if (result != null) {
					if (log.isDebugEnabled()) {
						log.debug("Level 2 cache hit: " + key);
					}
					level1.put(key, result);
				}
			}
		} else {
			result = (T) getMemcache(namespace).get(key);
			if (log.isDebugEnabled() && result != null) {
				log.debug("Level 2 cache hit: " + key);
			}
		}
		return result;
	}

    @Override
    public void clear(String namespace) {
        Preconditions.checkArgument(namespace != null, "Namespace is null");
        getMemcache(namespace).clearAll();
        Level1Cache level1 = Level1Cache.getCacheInstance();
        if (level1 != null) {
            level1.clear();
        }
    }

    @Override
	public void put(String memcacheNamespace, String key, Object value, int seconds) {
		Level1Cache level1 = Level1Cache.getCacheInstance();
		if (level1 != null) {
			level1.put(key, value);
		}
		if (seconds > 0) {
			getMemcache(memcacheNamespace).put(key, value, Expiration.byDeltaSeconds(seconds));
		}
	}

    private MemcacheService getMemcache(String memcacheNamespace) {
        return MemcacheServiceFactory.getMemcacheService(memcacheNamespace);
    }

	public void setPersistenceMetadataRepository(
			PersistenceMetadataRepository persistenceMetadataRepository) {
		this.persistenceMetadataRepository = persistenceMetadataRepository;
	}

}
