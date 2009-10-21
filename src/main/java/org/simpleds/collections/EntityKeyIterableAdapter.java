package org.simpleds.collections;

import java.util.Iterator;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

/**
 * Transforms a Iterable<Entity> into an Iterable<Key>
 * @author icoloma
 */
public class EntityKeyIterableAdapter implements Iterable<Key> {

	private Iterable<Entity> entities;
	
	public EntityKeyIterableAdapter(Iterable<Entity> entities) {
		this.entities = entities;
	}

	@Override
	public Iterator<Key> iterator() {
		return new EntityKeyIteratorAdapter(entities.iterator());
	}
	
	private class EntityKeyIteratorAdapter implements Iterator<Key> {

		private Iterator<Entity> entitiesIterator;
		
		public EntityKeyIteratorAdapter(Iterator<Entity> entitiesIterator) {
			super();
			this.entitiesIterator = entitiesIterator;
		}

		@Override
		public boolean hasNext() {
			return entitiesIterator.hasNext();
		}

		@Override
		public Key next() {
			return entitiesIterator.next().getKey();
		}

		@Override
		public void remove() {
			entitiesIterator.remove();
		}
		
	}
	
}
