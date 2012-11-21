package org.simpleds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.simpleds.functions.EntityToPropertyFunction;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Key;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A limited list of results with the cursor to resume the query where it was stopped.
 * @author icoloma
 * @param <J> the type of the list items
 */
public class CursorList<J> {

	/** the data corresponding to the current page of results */
	private List<J> data;
	
	/** the cursor to resume the current query */
	private Cursor cursor;
	
	/** the provided SimpleQuery object */
	private SimpleQuery query;
	
	private CursorList() {
		// empty 
	}
	
	private CursorList(List<J> data, Cursor cursor) {
		this.data = data;
		this.cursor = cursor;
	}
	
	/**
	 * Load the list of related entities.
	 * @param propertyName the name of the properties to be used as Key
	 * @return the list of retrieved entities
	 */
	public Map<Key, Object> loadRelatedEntities(String... propertyName) {
		EntityManager entityManager = EntityManagerFactory.getEntityManager();
		Class<J> persistentClass = (Class<J>) query.getClassMetadata().getPersistentClass();
		Set<Key> keys = Sets.newHashSet();
		for (String p : propertyName) {
			keys.addAll(Collections2.transform(data, new EntityToPropertyFunction(persistentClass, p)));
		}
		return entityManager.get(keys);
	}

	public static <J> CursorList<J> create(SimpleQuery query, int size) {
		return create(query, size, false);
	}
	/**
	 * Create a new CursorList instance. 
	 * @param query the query to execute
	 * @param size the size of the CursorList data. To retrieve more data, use query.withStartCursor()
	 * @param forceCursor if true, the cursor will be not null even on the last page of results. Useful to check for new contents after the last page has been retrieved.
	 * @return the created {@link CursorList} instance
	 */
	public static <J> CursorList<J> create(SimpleQuery query, int size, boolean cursorOnLastPage) {
		query.withPrefetchSize(size);
		CursorList<J> result = new CursorList<J>();
		result.query = query;
		result.data = Lists.newArrayListWithCapacity(size);
		SimpleQueryResultIterator<J> it = query.asIterator();
		for (int i = 0; it.hasNext() && i < size; i++) {
			result.data.add(it.next());
		}
		if (it.hasNext() || cursorOnLastPage) {
			result.cursor = it.getCursor();
		}
		return result;
	}

	/**
	 * Transform this {@link CursorList} instance by applying the transformation function to each data list item.
	 * Notice that the cursor instance will not be modified, so it will be usable only in the context of the original query.
	 * @param <O> the type of the resulting {@link CursorList}, after applying the transformation
	 * @param function the {@link Function} to apply to each data item
	 * @return a new {@link CursorList} instance. The original instance is not modified.
	 */
	public <O> CursorList<O> transform(Function<? super J, ? extends O> function) {
		return transform(function, null);
	}
	
	/**
	 * Transform this {@link CursorList} instance by applying the transformation function to each data list item.
	 * Notice that the cursor instance will not be modified, so it will be usable only in the context of the original query.
	 * @param <O> the type of the resulting {@link CursorList}, after applying the transformation
	 * @param function the function to apply to each data item. May be null.
	 * @param predicate the {@link Predicate} to filter returned data. Not-matching entities will not be returned. May be null.
	 * @return a new {@link CursorList} instance. The original instance is not modified.
	 */
	public <O> CursorList<O> transform(Function<? super J, ? extends O> function, Predicate<? super O> predicate) {
		Collection<O> result = null;
		if (function != null) {
			result = Lists.transform(this.data, function);
		} else {
			result = (List) this.data;
		}
		if (predicate != null) {
			result = Collections2.filter(result, predicate);
		}
		
		// copy the source data, since "live" collections are incompatible with paged results.
		ArrayList<O> dataCopy = Lists.newArrayList(result);
		CursorList<O> copy = new CursorList<O>(dataCopy, this.cursor);
		return copy;
	}

	/**
	 * Transform this {@link CursorList} of Keys to a similar {@link CursorList} of persistent entities, 
	 * using a single batch call to retrieve all the entities. 
	 * Notice that the cursor instance will not be modified, so it will be usable only in the context of the original query.
	 * @param <O> the type of the resulting {@link CursorList} after retrieving the persistent entities
	 * @return a new {@link CursorList} instance. The original instance is not modified.
	 */
	@SuppressWarnings("unchecked")
	public <O> CursorList<O> transformToEntities() {
		final Map<Key, O> valuesMap = EntityManagerFactory.getEntityManager().get((List<Key>)data);
		List<O> entities = Lists.transform((List<Key>)data, new Function<Key, O>() {

			@Override
			public O apply(Key key) {
				return valuesMap.get(key);
			}
			
		});
		CursorList<O> copy = new CursorList<O>(entities, this.cursor);
		return copy;
	}
	
	/**
	 * Remove null values from this collection
	 * Be aware that this may result in smaller page size
	 */
	public CursorList<J> filterNullValues() {
		for (Iterator<J> it = data.iterator(); it.hasNext(); ) {
			if (it.next() == null) {
				it.remove();
			}
		}
		return this;
	}

	public List<J> getData() {
		return data;
	}

	public Cursor getCursor() {
		return cursor;
	}
	
}
