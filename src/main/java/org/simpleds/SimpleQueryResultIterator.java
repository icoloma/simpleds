package org.simpleds;

import java.util.Iterator;

import com.google.appengine.api.datastore.Cursor;


/**
 * Equivalent to QueryResultIterator, but returns converted instances instead of Entities.
 * @author icoloma
 *
 * @param <T>
 */
public interface SimpleQueryResultIterator<T> extends Iterator<T> {
	
	/**
	 * @return the Cursor instance to continue iterating at this point. See QueryResultIterator javadoc for more details.
	 */
	Cursor getCursor();
	
}
