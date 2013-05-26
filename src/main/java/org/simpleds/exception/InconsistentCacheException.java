package org.simpleds.exception;

/**
 * Inconsistent cache state. An entity key in the cache references an entity that was not found in the datastore.
 * This is usually a human error produced by deleting an entity in the Datastore console and forgetting to empty memcache.
 * To fix this, just empty memcache or wait until the cache reference expires.
 */
public class InconsistentCacheException extends PersistenceException {

    public InconsistentCacheException(String message) {
        super(message);
    }

}
