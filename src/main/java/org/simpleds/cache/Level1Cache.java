package org.simpleds.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** 
 * The thread-bound Level 1 cache container.
 * This cache will store Key-entity pair values or String-Query data (be it a query count or a list of returning Key values).
 * @author Nacho
 *
 */
public class Level1Cache {
	
	/** the thread-bound instance */
	private static ThreadLocal<Level1Cache> threadLocal = new ThreadLocal<Level1Cache>();

	/** the cache contents (the key can be a Key or a String) */
	private Cache<Serializable, Object> contents;
	
	private static Logger log = LoggerFactory.getLogger(Level1Cache.class);
	
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

    public Level1Cache() {
        this.initCache();
    }

    /**
     * Initializes the Level 1 cache. Override to customize the cache parameters.
     * A Level1 cache instance is consumed by a single thread and will be cleared after processing
     * the current request, so overriding this method is usually not needed. It should be considered
     * only if you are using Task Queues or Backends that work with a lot of cached data.
     * @return a Cache implementation with a maximum of 1000 entities and 1-minute timeout.
     */
    protected void initCache() {
        contents = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

	@SuppressWarnings("unchecked")
	public <T> T get(Serializable key) {
		T value = (T) contents.getIfPresent(key);
		if (log.isDebugEnabled() && value != null) {
			log.debug("Level 1 cache hit: " + key);
		}
		return value;
	}

	public void put(Serializable key, Object instance) {
		contents.put(key, instance);
	}

	public void delete(Serializable key) {
		contents.invalidate(key);
	}

	public void delete(Collection<? extends Serializable> keys) {
        contents.invalidateAll(keys);
		if (log.isDebugEnabled()) {
			log.debug("Deleted from Level 1 cache: " + keys);
		}
	}

    /**
     * Return the list of values from cache. Only entries with non-null value will be returned
     */
	@SuppressWarnings("unchecked")
	public <T> Map<Serializable, T> get(Collection<? extends Serializable> keys) {
		Map<Serializable, T> result = Maps.newHashMapWithExpectedSize(keys.size());
		for (Serializable key : keys) {
			T value = (T) contents.getIfPresent(key);
			if (value != null) {
				result.put(key, value);
			}
		}
		if (log.isDebugEnabled() && !result.isEmpty()) {
			log.debug("Level 1 cache multiple hit: {}", result.keySet());
		}
		return result;
	}

	public <T> void put(Collection<? extends Serializable> keys, Collection<T> javaObjects) {
		Iterator<? extends Serializable> itKey = keys.iterator();
		Iterator<T> itJava = javaObjects.iterator();
		while (itKey.hasNext()) {
			contents.put(itKey.next(), itJava.next());
		}
	}

    public void clear() {
        this.contents.invalidateAll();
    }
	
}
