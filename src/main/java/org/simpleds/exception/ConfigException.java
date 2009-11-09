package org.simpleds.exception;

/**
 * Error in the persistence configuration
 * @author Nacho Coloma
 */
public class ConfigException extends RuntimeException {

	public ConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigException(String message) {
		super(message);
	}

	public ConfigException(Throwable cause) {
		super(cause);
	}

}
