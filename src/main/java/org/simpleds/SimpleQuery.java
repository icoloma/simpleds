package org.simpleds;

import java.util.List;

import org.simpleds.converter.CollectionConverter;
import org.simpleds.converter.Converter;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PropertyMetadata;
import org.springframework.util.ClassUtils;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.SortPredicate;

/**
 * Provides an easy way to create a Datastore {@link Query} 
 * and its associated {@link FetchOptions} instance.
 * @author icoloma
 */
public class SimpleQuery implements Cloneable {

	/** the {@link ClassMetadata} that corresponds to this query */
	private ClassMetadata classMetadata;
	
	/** the constructed query */
	private Query query;

	/** the fetch options */
	private FetchOptions fetchOptions;
	
	/** the transaction to use, null if none */
	private Transaction transaction;
	
	SimpleQuery(Key ancestor, ClassMetadata metadata) {
		this.classMetadata = metadata;
		this.query = new Query(metadata.getKind(), ancestor);
	}
	
	@Override
	public SimpleQuery clone() {
		SimpleQuery copy = new SimpleQuery(query.getAncestor(), classMetadata);
		for (FilterPredicate fpredicate : query.getFilterPredicates()) {
			copy.query.addFilter(fpredicate.getPropertyName(), fpredicate.getOperator(), fpredicate.getValue());
		}
		for (SortPredicate spredicate : query.getSortPredicates()) {
			copy.query.addSort(spredicate.getPropertyName(), spredicate.getDirection());
		}
		if (fetchOptions != null) {
			if (fetchOptions.getChunkSize() != null) {
				copy.withChunkSize(fetchOptions.getChunkSize());
			}
			if (fetchOptions.getLimit() != null) {
				copy.withLimit(fetchOptions.getLimit());
			}
			if (fetchOptions.getOffset() != null) {
				copy.withOffset(fetchOptions.getOffset());
			}
			if (fetchOptions.getPrefetchSize() != null) {
				copy.withPrefetchSize(fetchOptions.getPrefetchSize());
			}
		}
		return copy;
	}
	
	public String getKind() {
		return classMetadata.getKind();
	}
	
	public SimpleQuery addFilter(String propertyName, FilterOperator operator, Object value) {
		if (value != null) {
			
			// convert from java to google datastore
			Object convertedValue;
			Class expectedClass;
			if ("__key__".equals(propertyName)) {
				convertedValue = value;
				expectedClass = Key.class;
			} else {
				PropertyMetadata propertyMetadata = classMetadata.getProperty(propertyName);
				if (!propertyMetadata.isIndexed()) {
					throw new IllegalArgumentException(propertyName + " is not indexed. Correct your query, or remove @Unindexed and update your existing entities accordingly.");
				}
				Converter converter = propertyMetadata.getConverter();
				if (converter instanceof CollectionConverter) {
					convertedValue = ((CollectionConverter)converter).itemJavaToDatastore(value);
					expectedClass = ((CollectionConverter)converter).getItemType();
				} else {
					convertedValue = converter.javaToDatastore(value);
					expectedClass = propertyMetadata.getPropertyType();
				}
			}
			if (!ClassUtils.isAssignable(expectedClass, value.getClass())) {
				throw new IllegalArgumentException("Value of " + propertyName + " has wrong type. Expected " + expectedClass.getSimpleName() + ", but the query provided " + value.getClass().getSimpleName());
			}
			
			query.addFilter(propertyName, operator, convertedValue);
		}
		return this;
	}
	
	public SimpleQuery equal(String propertyName, Object value) {
		return addFilter(propertyName, FilterOperator.EQUAL, value);
	}
	
	public SimpleQuery isNull(String propertyName) {
		// check that the property exists
		classMetadata.getProperty(propertyName);
		query.addFilter(propertyName, FilterOperator.EQUAL, null);
		return this;
	}
	
	public SimpleQuery isNotNull(String propertyName) {
		// check that the property exists
		classMetadata.getProperty(propertyName);
		query.addFilter(propertyName, FilterOperator.GREATER_THAN, null);
		return this;
	}
	
	public SimpleQuery greaterThan(String propertyName, Object value) {
		return addFilter(propertyName, FilterOperator.GREATER_THAN, value);
	}
	
	public SimpleQuery greaterThanOrEqual(String propertyName, Object value) {
		return addFilter(propertyName, FilterOperator.GREATER_THAN_OR_EQUAL, value);
	}
	
	public SimpleQuery lessThan(String propertyName, Object value) {
		return addFilter(propertyName, FilterOperator.LESS_THAN, value);
	}
	
	public SimpleQuery lessThanOrEqual(String propertyName, Object value) {
		return addFilter(propertyName, FilterOperator.LESS_THAN_OR_EQUAL, value);
	}
	
	public SimpleQuery notEqual(String propertyName, Object value) {
		return addFilter(propertyName, FilterOperator.NOT_EQUAL, value);
	}
	
	/**
	 * Adds a LIKE clause. This like clause will only match strings that START
	 * with the provided argument. In other words, this clause will match "foo%" 
	 * but not "%foo%"
	 * @param propertyName the name of the property
	 * @param value the value of the property, without any '%' character.
	 */
	public SimpleQuery like(String propertyName, String value) {
		if (value != null) {
			this.greaterThanOrEqual(propertyName, value);
			this.lessThan(propertyName, value + '\ufffd');
		}
		return this;
	}
	
	public SimpleQuery sortAsc(String propertyName) {
		return sort(propertyName, SortDirection.ASCENDING);
	}
	
	/**
	 * @deprecated use sortAsc instead
	 */
	@Deprecated
	public SimpleQuery orderAsc(String propertyName) {
		return sortAsc(propertyName);
	}
	
	public SimpleQuery sort(String propertyName, SortDirection direction) {
		if (!"__key__".equals(propertyName)) {
			// check that the sort property exists
			classMetadata.getProperty(propertyName);
		}
		
		query.addSort(propertyName, direction);
		return this;
	}
	
	/**
	 * @deprecated use sortDesc instead
	 */
	@Deprecated
	public SimpleQuery orderDesc(String propertyName) {
		return sortDesc(propertyName);
	}
	
	public SimpleQuery sortDesc(String propertyName) {
		return sort(propertyName, SortDirection.DESCENDING);
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
	
	public SimpleQuery withCursor(Cursor cursor) {
		fetchOptions = fetchOptions == null? FetchOptions.Builder.withCursor(cursor) : fetchOptions.cursor(cursor);
		return this;
	}
	
	/**
	 * Convenience method that acts as withCursor(Cursor), but accepts a cursor serialized as String 
	 */
	public SimpleQuery withCursor(String cursor) {
		return withCursor(Cursor.fromWebSafeString(cursor));
	}
	
	/**
	 * Specify the transaction to use when executing this query
	 * @param transaction the transaction to use (can be null)
	 */
	public SimpleQuery withTransaction(Transaction transaction) {
		this.transaction = transaction;
		return this;
	}

	public List<FilterPredicate> getFilterPredicates() {
		return query.getFilterPredicates();
	}

	public List<SortPredicate> getSortPredicates() {
		return query.getSortPredicates();
	}
	
	public boolean isKeysOnly() {
		return query.isKeysOnly();
	}
	
	public Query getQuery() {
		return query;
	}

	public FetchOptions getFetchOptions() {
		return fetchOptions;
	}

	public ClassMetadata getClassMetadata() {
		return classMetadata;
	}

	public Transaction getTransaction() {
		return transaction;
	}

}
