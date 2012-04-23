package org.simpleds;

import org.simpleds.metadata.ClassMetadata;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.common.base.Predicate;

class SimpleQueryResultIterableImpl<T> implements SimpleQueryResultIterable<T> {

	/** the classMetadata used to transform each Entity into the corresponding Java type */
	private ClassMetadata metadata;
	
	/** the underlying Iterable instance */
	private QueryResultIterable<Entity> iterable;
	
	/** true to return keys only, false otherwise */
	private boolean keysOnly;

	/** if not null, predicate will be used to filter the returned collection using Java code */
	private Predicate<T> predicate;
	
	SimpleQueryResultIterableImpl(ClassMetadata metadata, Predicate<T> predicate, QueryResultIterable<Entity> iterable) {
		this.metadata = metadata;
		this.iterable = iterable;
		this.predicate = predicate;
	}

	@Override
	public SimpleQueryResultIterator<T> iterator() {
		return predicate == null? new SimpleQueryResultIteratorImpl<T>(metadata, iterable.iterator()).setKeysOnly(keysOnly)
				: new PredicateSimpleQueryResultIteratorImpl<T>(metadata, predicate, iterable.iterator()).setKeysOnly(keysOnly);
	}
	
	public SimpleQueryResultIterableImpl<T> setKeysOnly(boolean keysOnly) {
		this.keysOnly = keysOnly;
		return this;
	}
	
}
