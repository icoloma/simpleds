package org.simpleds;

import com.google.appengine.api.datastore.Cursor;


/**
 * Equivalent to QueryResultIterable, but returns converted instances instead of Entities.
 * @author icoloma
 *
 * @param <T>
 */
public interface SimpleQueryResultIterable<T> extends Iterable<T> {
	
	public abstract SimpleQueryResultIterator<T> iterator();
	
	/**
	 * @return the Cursor instance to continue iterating at this point. See QueryResultIterator javadoc for more details.
	 */
	Cursor getCursor();
	
}
