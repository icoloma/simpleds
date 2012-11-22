package org.simpleds;



/**
 * Equivalent to QueryResultIterable, but returns converted instances instead of Entities.
 * @author icoloma
 *
 * @param <T>
 */
public interface CursorIterable<T> extends Iterable<T> {
	
	public abstract CursorIterator<T> iterator();
	
}
