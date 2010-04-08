package org.simpleds;


/**
 * Equivalent to QueryResultIterable, but returns converted instances instead of Entities.
 * @author icoloma
 *
 * @param <T>
 */
public interface SimpleQueryResultIterable<T> extends Iterable<T> {
	
	public abstract SimpleQueryResultIterator<T> iterator();
	
}
