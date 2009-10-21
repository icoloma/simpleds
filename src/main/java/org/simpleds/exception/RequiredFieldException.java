package org.simpleds.exception;

/**
 * A required property was missing when trying to persist an entity
 * @author icoloma
 */
public class RequiredFieldException extends PersistenceException {

	public RequiredFieldException() {
		super();
	}

	public RequiredFieldException(String message, Throwable cause) {
		super(message, cause);
	}

	public RequiredFieldException(String message) {
		super(message);
	}

	public RequiredFieldException(Throwable cause) {
		super(cause);
	}

}
