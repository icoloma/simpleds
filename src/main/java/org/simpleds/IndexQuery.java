package org.simpleds;

import org.simpleds.metadata.MultivaluedIndexMetadata;

import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;

/**
 * Query for a multivalued index. This query will return instances of the entity containing the index, instead of the index itself
 * @author icoloma
 */
public class IndexQuery {

	/** the constructed query */
	private Query query;

	/** the fetch options */
	private FetchOptions fetchOptions;
	
	/** if true, return only the keys of the containing instances (default false) */
	private boolean keysOnly;
	
	private MultivaluedIndexMetadata metadata;
	
	public IndexQuery(MultivaluedIndexMetadata index) {
		query = new Query(index.getKind());
		query.setKeysOnly();
		this.metadata = index;
	}

	public IndexQuery equal(Object value) {
		if (value != null) {
			query.addFilter("contents", FilterOperator.EQUAL, value);
		}
		return this;
	}
	
	public IndexQuery greaterThan(Object value) {
		if (value != null) {
			query.addFilter("contents", FilterOperator.GREATER_THAN, value);
		}
		return this;
	}
	
	public IndexQuery greaterThanOrEqual(Object value) {
		if (value != null) {
			query.addFilter("contents", FilterOperator.GREATER_THAN_OR_EQUAL, value);
		}
		return this;
	}
	
	public IndexQuery lessThan(Object value) {
		if (value != null) {
			query.addFilter("contents", FilterOperator.LESS_THAN, value);
		}
		return this;
	}
	
	public IndexQuery lessThanOrEqual(Object value) {
		if (value != null) {
			query.addFilter("contents", FilterOperator.LESS_THAN_OR_EQUAL, value);
		}
		return this;
	}
	
	/**
	 * If true, this return will only return the indexes of the containing instances of the provided index
	 * @return
	 */
	public IndexQuery keysOnly() {
		this.keysOnly = true;
		return this;
	}
	
	public IndexQuery withLimit(int limit) {
		fetchOptions = fetchOptions == null? FetchOptions.Builder.withLimit(limit) : fetchOptions.limit(limit);
		return this;
	}
	
	public IndexQuery withPrefetchSize(int size) {
		fetchOptions = fetchOptions == null? FetchOptions.Builder.withPrefetchSize(size) : fetchOptions.prefetchSize(size);
		return this;
	}
	
	public IndexQuery withChunkSize(int size) {
		fetchOptions = fetchOptions == null? FetchOptions.Builder.withChunkSize(size) : fetchOptions.chunkSize(size);
		return this;
	}
	
	public IndexQuery withOffset(int offset) {
		fetchOptions = fetchOptions == null? FetchOptions.Builder.withOffset(offset) : fetchOptions.offset(offset);
		return this;
	}
	
	public Query getQuery() {
		return query;
	}

	public FetchOptions getFetchOptions() {
		return fetchOptions;
	}

	public boolean isKeysOnly() {
		return keysOnly;
	}

	public MultivaluedIndexMetadata getMetadata() {
		return metadata;
	}

}
