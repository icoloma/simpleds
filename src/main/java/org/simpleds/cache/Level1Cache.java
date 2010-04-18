package org.simpleds.cache;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.common.collect.Maps;

/** 
 * The thread-bound Level 1 cache container.
 * @author Nacho
 *
 */
class Level1Cache implements CacheManager {
	
	/** the thread-bound instance */
	private static ThreadLocal<Level1Cache> threadLocal = new ThreadLocal<Level1Cache>();

	/** the cache contents */
	private Map<Key, Object> contents = new WeakHashMap<Key, Object>();
	
	/**
	 * Initializes the Level 1 cache for this thread.
	 * This method should be invoked at the beginning of processing this request.
	 */
	public static void initialize() {
		threadLocal.set(new Level1Cache());
	}
	
	/**
	 * Clears the Level1Cache associated to this thread
	 * This method should be invoked at the end of processing this request.
	 */
	public static void clear() {
		threadLocal.remove();
	}
	
	/**
	 * Return the Level1Cache associated to this thread.
	 * @return the Level1Cache associated to this thread. May be null.
	 */
	public static Level1Cache get() {
		return threadLocal.get();
	}

	@Override
	public <T> T get(Key key) {
		return (T) contents.get(key);
	}

	@Override
	public void put(Object instance, Entity entity) {
		contents.put(entity.getKey(), instance);
	}

	@Override
	public void delete(Key key) {
		contents.remove(key);
	}

	@Override
	public void delete(Collection<Key> keys) {
		for (Key key : keys) {
			contents.remove(key);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Map<Key, T> get(Collection<Key> keys) {
		Map<Key, T> result = Maps.newHashMapWithExpectedSize(keys.size());
		for (Key key : keys) {
			T value = (T) contents.get(key);
			if (value != null) {
				result.put(key, value);
			}
		}
		return result;
	}

	@Override
	public void put(Collection javaObjects, List<Entity> entities) {
		Iterator<Entity> itEnt = entities.iterator();
		Iterator<Object> itJava = javaObjects.iterator();
		while (itEnt.hasNext()) {
			contents.put(itEnt.next().getKey(), itJava.next());
		}
	}
	
}
