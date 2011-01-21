package org.simpleds.exception;

import org.simpleds.annotations.Version;

import com.google.appengine.api.datastore.Key;

/**
 * Thrown by EntityManager.put(tx, entity) when the {@link Version} attribute does not match.
 * @author icoloma
 *
 */
public class OptimisticLockException extends PersistenceException {

	public OptimisticLockException(Key key, Object expectedVersion, Object actualVersion) {
		super("Optimistic lock conflict trying to persist " + key + ". Found " + expectedVersion + ", expected " + actualVersion);
	}
	
}
