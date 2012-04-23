package org.simpleds;

import java.util.NoSuchElementException;

import org.simpleds.metadata.ClassMetadata;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.common.base.Predicate;

/**
 * Iterator with {@link Predicate}. Being able to filter in memory makes this implementation much more complicated.
 * @author icoloma
 *
 * @param <T>
 */
class PredicateSimpleQueryResultIteratorImpl<T> implements SimpleQueryResultIterator<T> {

	/** the class metadata to transform datastore Entities to java and viceversa */ 
	private ClassMetadata metadata;
	
	/** the underlying iterator to use */
	private QueryResultIterator<Entity> iterator;
	
	/** true to return keys only instead of full-blown entities */
	private boolean keysOnly;
	
	/** predicate will be used to filter the returned collection using Java code */
	private Predicate<T> predicate;
	
	/** the current element that will be returned by next() */
	private T nextElement;
	
	/** cursor previous to the current element */
	private Cursor cursor;
	
	PredicateSimpleQueryResultIteratorImpl(ClassMetadata metadata, Predicate<T> predicate, QueryResultIterator<Entity> iterator) {
		this.metadata = metadata;
		this.predicate = predicate;
		this.iterator = iterator;
		retrieveNextElement();
	}
	
	@SuppressWarnings("unchecked")
	private void retrieveNextElement() {
		while (iterator.hasNext()) {
			cursor = iterator.getCursor();
			Entity entity = iterator.next();
			nextElement = keysOnly? (T) entity.getKey() : (T) metadata.datastoreToJava(entity);
			if (predicate.apply(nextElement)) {
				return;
			}
		}
		// has reached the end
		nextElement = null;
	}

	@Override
	public Cursor getCursor() {
		return cursor;
	}

	@Override
	public boolean hasNext() {
		return nextElement != null;
	}

	@Override
	public T next() {
		if (nextElement == null) {
			throw new NoSuchElementException();
		}
		T currentElement = nextElement;
		retrieveNextElement();
		return currentElement;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove() is not allowed with Predicates");
	}
	
	public PredicateSimpleQueryResultIteratorImpl<T> setKeysOnly(boolean keysOnly) {
		this.keysOnly = keysOnly;
		return this;
	}

}
