package org.simpleds;

import java.util.Collection;
import java.util.List;

import org.simpleds.annotations.MultivaluedIndex;

import com.google.appengine.api.datastore.Key;

/**
 * Manages MultivaluedIndex instances
 * @author icoloma
 *
 */
@SuppressWarnings("unchecked")
public interface IndexManager {

	/**
	 * Retrieves the list of values stored for this MultivaluedIndex
	 */
	<T extends Collection> T get(Key entityKey, String indexName);

	/**
	 * Stores the list of values stored for this MultivaluedIndex.
	 * When schema contraints validation is on, this method will check only 
	 * the first value of the collection because we expect that all the collection values 
	 * share the same class.
	 */
	void put(Key entityKey, String indexName, Collection indexValues);

	/**
	 * Adds the provided value to the list of values stored for this MultivaluedIndex
	 * 
	 * @param entityKey the key of the entity containing this index
	 * @param indexName the name of the index, as specified in a {@link MultivaluedIndex} annotation
	 * @param indexValue the value to add to this index. If the index does already contain
	 * this value, the call will succeed anyway
	 * @return the collection of index values stored in the database after adding the value.
	 */
	<T extends Collection> T addIndexValue(Key entityKey, String indexName, Object indexValue);

	/**
	 * Removes the provided value from the list of values stored for this MultivaluedIndex,
	 * and store sthe updated value
	 * 
	 * @param entityKey the key of the entity containing this index
	 * @param indexName the name of the index, as specified in a {@link MultivaluedIndex} annotation
	 * @param indexValue the value to remove from this index. If the index does not contain
	 * this value, the call will succeed anyway
	 * @return the collection of index values stored in the database after removing the value.
	 */
	<T extends Collection> T deleteIndexValue(Key entityKey, String indexName, Object indexValue);

	/**
	 * Create a new IndexQuery
	 * @param entityClazz the persistent entity containing the index
	 * @param indexName the name of the index
	 * @return a IndexQuery for the provided index
	 */
	IndexQuery newQuery(Class entityClazz, String indexName);

	/**
	 * Return a list of persistent instances that satisfy the provided {@link IndexQuery}
	 * @param <T>
	 * @param factory
	 * @return
	 */
	<T> List<T> find(IndexQuery factory);
	
}
