package org.simpleds.exception;

/**
 * Triggered when an entity could not be found in the Datastore
 * @author icoloma
 */
public class EntityNotFoundException extends PersistenceException {

	public EntityNotFoundException() { /* */ }
	
	public EntityNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	public EntityNotFoundException(String message) {
		super(message);
	}
	public EntityNotFoundException(Throwable cause) {
		super(cause);
	}
	
	
}
