package org.simpleds;

import org.simpleds.metadata.ClassMetadata;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultIterator;

class SimpleQueryResultIteratorImpl<T> implements SimpleQueryResultIterator<T> {

	/** the class metadata to transform datastore Entities to java and viceversa */ 
	private ClassMetadata metadata;
	
	/** the underlying iterator to use */
	private QueryResultIterator<Entity> iterator;
	
	/** true to return keys only instead of full-blown entities */
	private boolean keysOnly;
	
	SimpleQueryResultIteratorImpl(ClassMetadata metadata,
			QueryResultIterator<Entity> iterator) {
		this.metadata = metadata;
		this.iterator = iterator;
	}

	@Override
	public Cursor getCursor() {
		return iterator.getCursor();
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	@SuppressWarnings("unchecked")
	public T next() {
		Entity entity = iterator.next();
		return keysOnly? (T) entity.getKey() : (T) metadata.datastoreToJava(entity);
	}

	@Override
	public void remove() {
		iterator.remove();
	}
	
	public SimpleQueryResultIteratorImpl<T> setKeysOnly(boolean keysOnly) {
		this.keysOnly = keysOnly;
		return this;
	}

}
