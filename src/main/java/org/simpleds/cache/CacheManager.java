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
 * Manages the caching of entities using a two level cache:
 * <ul>
 * <li>Level 1: Keeps track of entities retrieved by the current thread. 
 * If the required entity has been previously retrieved by the current thread, 
 * it is returned as is (no unmarshalling is required)</li>
 * <li>Level 2: Any entity that has not been returned by the level 1 cache 
 * will be tested against the level 2 cache, which is backed by memcache.
 * This level will use the {@link Entity} representation, so an unmarshall 
 * will be required (same as it would with any value returned by the datastore).
 * </ul>
 * 
 * @author Nacho
 *
 */
public interface CacheManager {

	/** memcache namespace for SimpleDS cache */
	static final String MEMCACHE_NAMESPACE = "_sds_cache";
	
	/**
	 * Check the level 1 and level 2 cache for the required value.
	 * If the value is found in Level 1 cache, it is returned as is.
	 * Otherwise, the Level 2 cache will be queried for the value. If found,
	 * the value will be injected into Level 1 and returned.
	 * If the value is not found in Level 1 or Level2, this method will return null.
	 * @param key the key of the persistent entity to return
	 * @return the cached value, or null if not found.
	 */
	<T> T get(Key key, ClassMetadata metadata);
	
	/**
	 * Put an instance in the cache
	 * @param instance the java object
	 * @param entity the datastore-equivalent Entity instance
	 * @param metadata the {@link ClassMetadata} instance for this entity
	 */
	void put(Object instance, Entity entity, ClassMetadata metadata);
	
	/**
	 * Remove an entity from the cache
	 * @param key the key to remove
	 */
	void delete(Key key);
	
	/**
	 * Retrieve a set of entities from the cache
	 * @param keys the keys to retrieve from the cache
	 * @return a Map of retrieved persistent entities.
	 */
	Map<Key, Object> get(Multimap<ClassMetadata, Key> keys);
	
	/**
	 * Return the cached query data, if available
	 * @param key the key to retrieve
	 * @return the cached query data, null if not cached
	 */
	<T> T get(String key);

	/**
	 * Put a collection of java objects in the cache
	 * @param javaObjects the persistent objects to put into the Level 1 cache
	 * @param dsEntities the list of entities to put into the Level 2 cache
	 */
	void put(ListMultimap<ClassMetadata, Object> javaObjects, ListMultimap<ClassMetadata, Entity> dsEntities);
	
	/**
	 * Store query data into the cache
	 * @param cacheKey the key of the query
	 * @param value the value to store
	 * @param seconds the number of seconds to store in the cache, 0 to use only the Level 1 cache
	 */
	void put(String cacheKey, Object value, int seconds);
	
	/**
	 * Delete contents from the cache
	 * @param keys the keys to remove
	 */
	void delete(Collection<? extends Serializable> keys);

}
