package org.simpleds.exception;

/**
 * Two classes with the same kind or two properties with the same name 
 * @author icoloma
 *
 */
public class DuplicateException extends ConfigException {

	public DuplicateException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateException(String message) {
		super(message);
	}
	
}
