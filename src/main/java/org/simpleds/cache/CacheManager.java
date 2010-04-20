package org.simpleds.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.simpleds.metadata.ClassMetadata;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

/**
 * Manages the caching of entities using a two level cache:
 * <ul>
 * <li>Level 1: Keeps track of entities retrieved by the current thread. 
 * If the required entity has been previously retrieved by the current thread, 
 * it is returned as is (no unmarshalling is required)</li>
 * <li>Level 2: Any entity that has not been returned by the level 1 cache 
 * will be tested against the level 2 cache, which is backed by memcache.
 * This level will use the {@link Entity} representation, so an unmarshall 
 * will be required (same as would with any value returned by the datastore).
 * </ul>
 * 
 * @author Nacho
 *
 */
public interface CacheManager {

	/** memcache namespace for SimpleDS cache */
	public static final String MEMCACHE_NAMESPACE = "_sds_cache";

	/**
	 * Check the level 1 and level 2 cache for the required value.
	 * If the value is found in Level 1 cache, it is returned as is.
	 * Otherwise, the Level 2 cache will be queried for the value. If found,
	 * the value will be injected into Level 1 and returned.
	 * If the value is not found in Level 1 or Level2, this method will return null.
	 * @param key the key of the persistent entity to return
	 * @return the cached value, or null if not found.
	 */
	public <T> T get(Key key, ClassMetadata metadata);
	
	/**
	 * Put an instance in the cache
	 * @param instance the java object
	 * @param entity the datastore-equivalent Entity instance
	 */
	public void put(Object instance, Entity entity, ClassMetadata metadata);
	
	/**
	 * Remove an entity from the cache
	 * @param key the key to remove
	 */
	public void delete(Key key);

	/**
	 * Retrieve a set of entities from the cache
	 * @param keys the keys to retrieve from the cache
	 * @return a Map of retrieved persistent entities.
	 */
	public <T> Map<Key, T> get(Collection<Key> keys, ClassMetadata metadata);

	/**
	 * Delete a set of entities from the cache
	 * @param keys
	 */
	public void delete(Collection<Key> keys);

	/**
	 * Put a collection of java objects in the cache
	 * @param javaObjects the persistent objects to put into the Level 1 cache
	 * @param entities the list of entities to put into the Level 2 cache
	 */
	public <T> void put(Collection<T> javaObjects, List<Entity> entities, ClassMetadata metadata);

	
}
