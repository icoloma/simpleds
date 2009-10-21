package org.simpleds;

import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;

/**
 * Provides an easy way to create a Datastore {@link Query} 
 * and its associated {@link FetchOptions} instance.
 * @author icoloma
 */
public class SimpleQuery {

	/** the constructed query */
	private Query query;

	/** the fetch options */
	private FetchOptions fetchOptions;
	
	public SimpleQuery(Key ancestor) {
		query = new Query(ancestor);
	}
	
	public SimpleQuery(String kind) {
		query = new Query(kind);
	}
	
	public SimpleQuery(Class clazz) {
		query = new Query(clazz.getSimpleName());
	}
	
	public SimpleQuery(Key ancestor, String kind) {
		query = new Query(kind, ancestor);
	}
	
	public SimpleQuery(Key ancestor, Class clazz) {
		query = new Query(clazz.getSimpleName(), ancestor);
	}
	
	public SimpleQuery equal(String propertyName, Object value) {
		if (value != null) {
			query.addFilter(propertyName, FilterOperator.EQUAL, value);
		}
		return this;
	}
	
	public SimpleQuery addFilter(String propertyName, FilterOperator operator, Object value) {
		if (value != null) {
			query.addFilter(propertyName, operator, value);
		}
		return this;
	}
	
	public SimpleQuery isNull(String propertyName) {
		query.addFilter(propertyName, FilterOperator.EQUAL, null);
		return this;
	}
	
	public SimpleQuery isNotNull(String propertyName) {
		query.addFilter(propertyName, FilterOperator.GREATER_THAN, null);
		return this;
	}
	
	public SimpleQuery greaterThan(String propertyName, Object value) {
		if (value != null) {
			query.addFilter(propertyName, FilterOperator.GREATER_THAN, value);
		}
		return this;
	}
	
	public SimpleQuery greaterThanOrEqual(String propertyName, Object value) {
		if (value != null) {
			query.addFilter(propertyName, FilterOperator.GREATER_THAN_OR_EQUAL, value);
		}
		return this;
	}
	
	public SimpleQuery lessThan(String propertyName, Object value) {
		if (value != null) {
			query.addFilter(propertyName, FilterOperator.LESS_THAN, value);
		}
		return this;
	}
	
	public SimpleQuery lessThanOrEqual(String propertyName, Object value) {
		if (value != null) {
			query.addFilter(propertyName, FilterOperator.LESS_THAN_OR_EQUAL, value);
		}
		return this;
	}
	
	/**
	 * Adds a LIKE clause. This like clause will only match strings that START
	 * with the provided argument. In other words, this clause will match "foo%" 
	 * but not "%foo%"
	 * @param propertyName the name of the property
	 * @param value the value of the property, without any '%' character.
	 * @return
	 */
	public SimpleQuery like(String propertyName, String value) {
		if (value != null) {
			this.greaterThanOrEqual(propertyName, value);
			this.lessThan(propertyName, value + '\ufffd');
		}
		return this;
	}
	
	public SimpleQuery orderAsc(String propertyName) {
		query.addSort(propertyName, SortDirection.ASCENDING);
		return this;
	}
	
	public SimpleQuery order(String propertyName, SortDirection direction) {
		query.addSort(propertyName, direction);
		return this;
	}
	
	public SimpleQuery orderDesc(String propertyName) {
		query.addSort(propertyName, SortDirection.DESCENDING);
		return this;
	}
	
	public SimpleQuery keysOnly() {
		query.setKeysOnly();
		return this;
	}
	
	public SimpleQuery withLimit(int limit) {
		fetchOptions = fetchOptions == null? FetchOptions.Builder.withLimit(limit) : fetchOptions.limit(limit);
		return this;
	}
	
	public SimpleQuery withPrefetchSize(int size) {
		fetchOptions = fetchOptions == null? FetchOptions.Builder.withPrefetchSize(size) : fetchOptions.prefetchSize(size);
		return this;
	}
	
	public SimpleQuery withChunkSize(int size) {
		fetchOptions = fetchOptions == null? FetchOptions.Builder.withChunkSize(size) : fetchOptions.chunkSize(size);
		return this;
	}
	
	public SimpleQuery withOffset(int offset) {
		fetchOptions = fetchOptions == null? FetchOptions.Builder.withOffset(offset) : fetchOptions.offset(offset);
		return this;
	}
	
	public Query getQuery() {
		return query;
	}

	public FetchOptions getFetchOptions() {
		return fetchOptions;
	}

}