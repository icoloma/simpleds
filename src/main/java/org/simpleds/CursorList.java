package org.simpleds;

import java.util.ArrayList;
import java.util.List;

import org.simpleds.functions.EntityToPropertyFunction;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Key;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * A limited list of results with the cursor to resume the query where it was stopped.
 * @author icoloma
 * @param <J> the type of the list items
 */
public class CursorList<J> {

	/** the default size of CursorList, if none is specified using SimpleQuery.withLimit() */
	private static final int DEFAULT_SIZE = 10;
	
	private List<J> data;
	
	private Cursor cursor;
	
	private CursorList() {
		// empty 
	}
	
	private CursorList(List<J> data, Cursor cursor) {
		this.data = data;
		this.cursor = cursor;
	}
	
	/**
	 * Create a new CursorList instance. 
	 * @param query the query to execute
	 * @param size the size of the CursorList data. To retrieve more data, use query.withStartCursor()
	 * @return the created {@link CursorList} instance
	 */
	public static <J> CursorList<J> create(SimpleQuery query) {
		CursorList<J> result = new CursorList<J>();
		Integer chunkSize = query.getFetchOptions().getChunkSize();
		int size;
		if (chunkSize == null) {
			size = DEFAULT_SIZE;
			query.withChunkSize(size);
		} else {
			size = chunkSize;
		}
		result.data = Lists.newArrayListWithCapacity(size);
		SimpleQueryResultIterator<J> it = query.asIterator();
		for (int i = 0; it.hasNext() && i < size; i++) {
			result.data.add(it.next());
		}
		if (it.hasNext()) {
			result.cursor = it.getCursor();
		}
		return result;
	}
	
	public CursorList<J> loadRelations(Class<J> clazz, String propertyName) {
		EntityManager entityManager = EntityManagerFactory.getEntityManager();
		entityManager.get(Collections2.transform(data, new EntityToPropertyFunction(clazz, propertyName)));
		return this;
	}

	/**
	 * Transform this {@link CursorList} instance by applying the transformation function to each data list item.
	 * Notice that the cursor instance will not be modified, so it will be usable only in the context of the original query.
	 * @param <O> the type of the resulting {@link CursorList}, after applying the transformation
	 * @param function the function to apply to each data item
	 * @return a new {@link CursorList} instance. The original instance is not modified.
	 */
	public <O> CursorList<O> transform(Function<? super J, ? extends O> function) {
		// copy the source data, since "live" collections are incompatible with paged results.
		ArrayList<O> dataCopy = Lists.newArrayList(Lists.transform(this.data, function));
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
		List<O> entities = EntityManagerFactory.getEntityManager().get((List<Key>)data);
		CursorList<O> copy = new CursorList<O>(entities, this.cursor);
		return copy;
	}

	public List<J> getData() {
		return data;
	}

	public Cursor getCursor() {
		return cursor;
	}
	
}
