package org.simpleds.cache;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.appengine.api.datastore.Key;
import com.google.common.collect.Maps;

/** 
 * The thread-bound Level 1 cache container.
 * @author Nacho
 *
 */
class Level1Cache {
	
	/** the thread-bound instance */
	private static ThreadLocal<Level1Cache> threadLocal = new ThreadLocal<Level1Cache>();

	/** the cache contents */
	private Map<Key, Object> contents = new WeakHashMap<Key, Object>();
	
	private Log log = LogFactory.getLog(Level1Cache.class);
	
	/**
	 * Initializes the Level 1 cache for this thread.
	 * This method should be invoked at the beginning of processing this request.
	 */
	public static void setCacheInstance() {
		threadLocal.set(new Level1Cache());
	}
	
	/**
	 * Clears the Level1Cache associated to this thread
	 * This method should be invoked at the end of processing this request.
	 */
	public static void clearCacheInstance() {
		threadLocal.remove();
	}
	
	/**
	 * Return the Level1Cache associated to this thread.
	 * @return the Level1Cache associated to this thread. May be null.
	 */
	public static Level1Cache getCacheInstance() {
		return threadLocal.get();
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Key key) {
		T value = (T) contents.get(key);
		if (log.isDebugEnabled() && value != null) {
			log.debug("Level 1 cache hit: " + key);
		}
		return value;
	}

	public void put(Key key, Object instance) {
		contents.put(key, instance);
	}

	public void delete(Key key) {
		contents.remove(key);
	}

	public void delete(Collection<Key> keys) {
		for (Key key : keys) {
			contents.remove(key);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Map<Key, T> get(Collection<Key> keys) {
		Map<Key, T> result = Maps.newHashMapWithExpectedSize(keys.size());
		for (Key key : keys) {
			T value = (T) contents.get(key);
			if (value != null) {
				result.put(key, value);
			}
		}
		if (log.isDebugEnabled() && !result.isEmpty()) {
			log.debug("Level 1 cache multiple hit: " + result.keySet());
		}
		return result;
	}

	public <T> void put(Collection<Key> keys, Collection<T> javaObjects) {
		Iterator<Key> itKey = keys.iterator();
		Iterator<T> itJava = javaObjects.iterator();
		while (itKey.hasNext()) {
			contents.put(itKey.next(), itJava.next());
		}
	}
	
}