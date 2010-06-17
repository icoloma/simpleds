package org.simpleds;

import org.simpleds.metadata.ClassMetadata;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultIterable;

class SimpleQueryResultIterableImpl<T> implements SimpleQueryResultIterable<T> {

	/** the classMetadata used to transform each Entity into the corresponding Java type */
	private ClassMetadata metadata;
	
	/** the underlying Iterable instance */
	private QueryResultIterable<Entity> iterable;
	
	/** true to return keys only, false otherwise */
	private boolean keysOnly;
	
	SimpleQueryResultIterableImpl(ClassMetadata metadata, QueryResultIterable<Entity> iterable) {
		this.metadata = metadata;
		this.iterable = iterable;
	}

	@Override
	public SimpleQueryResultIterator<T> iterator() {
		return new SimpleQueryResultIteratorImpl<T>(metadata, iterable.iterator()).setKeysOnly(keysOnly);
	}
	
	public SimpleQueryResultIterableImpl<T> setKeysOnly(boolean keysOnly) {
		this.keysOnly = keysOnly;
		return this;
	}
	
}
