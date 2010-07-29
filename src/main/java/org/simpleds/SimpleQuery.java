package org.simpleds;

import java.util.Collection;
import java.util.List;

import org.simpleds.cache.CacheManager;
import org.simpleds.exception.EntityNotFoundException;
import org.simpleds.functions.EntityToKeyFunction;
import org.simpleds.metadata.ClassMetadata;
import org.simpleds.metadata.PropertyMetadata;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.SortPredicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Proxy class to handle a {@link Query} instance. 
 * @author icoloma
 */
public class SimpleQuery implements ParameterQuery, Cloneable {

	/** the {@link ClassMetadata} that corresponds to this query */
	private ClassMetadata classMetadata;
	
	/** the constructed query */
	private Query query;

	/** the fetch options */
	private FetchOptions fetchOptions;
	
	/** the transaction to use, null if none */
	private Transaction transaction;
	
	/** the {@link EntityManager} that created this instance */
	private EntityManager entityManager;
	
	/** the {@link DatastoreServiceConfig} instance to use, if any. If null, the value of entityManager.getDatastoreService() will be used */
	private DatastoreServiceConfig datastoreServiceConfig;
	
	/** the cache key to use. If null, the query results will not be cached */
	private String cacheKey;
	
	/** the number of seconds to store data in the memcache. Default (0) will only use the Level 1 cache */
	private int cacheSeconds;
	
	SimpleQuery(EntityManager entityManager, Key ancestor, ClassMetadata metadata) {
		this.entityManager = entityManager;
		this.classMetadata = metadata;
		this.query = new Query(metadata.getKind(), ancestor);
	}
	
	@Override
	public SimpleQuery clone() {
		SimpleQuery copy = new SimpleQuery(entityManager, query.getAncestor(), classMetadata);
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
		copy.withTransaction(transaction);
		copy.datastoreServiceConfig = datastoreServiceConfig;
		copy.cacheKey = cacheKey;
		copy.cacheSeconds = cacheSeconds;
		return copy;
	}
	
	@Override
	public String getKind() {
		return classMetadata.getKind();
	}
	
	@Override
	public SimpleQuery addFilter(String propertyName, FilterOperator operator, Object value) {
		if (value != null) {
			PropertyMetadata propertyMetadata = getPropertyMetadata(propertyName);
			query.addFilter(propertyName, operator, propertyMetadata.convertQueryParam(value));
		}
		return this;
	}
	
	/**
	 * @return the {@link PropertyMetadata} instance associated to the provided propertyName. Accepts "__key__"
	 */
	private PropertyMetadata getPropertyMetadata(String propertyName) {
		PropertyMetadata propertyMetadata = "__key__".equals(propertyName)? classMetadata.getKeyProperty() : classMetadata.getProperty(propertyName);
		if (!propertyMetadata.isIndexed()) {
			throw new IllegalArgumentException(propertyName + " is not indexed. Correct your query, or remove @Unindexed and update your existing entities accordingly.");
		}	
		return propertyMetadata;
	}

	@Override
	public SimpleQuery equal(String propertyName, Object value) {
		return addFilter(propertyName, FilterOperator.EQUAL, value);
	}
	
	@Override
	public SimpleQuery isNull(String propertyName) {
		// check that the property exists
		classMetadata.getProperty(propertyName);
		query.addFilter(propertyName, FilterOperator.EQUAL, null);
		return this;
	}
	
	@Override
	public SimpleQuery isNotNull(String propertyName) {
		// check that the property exists
		classMetadata.getProperty(propertyName);
		query.addFilter(propertyName, FilterOperator.GREATER_THAN, null);
		return this;
	}
	
	@Override
	public SimpleQuery greaterThan(String propertyName, Object value) {
		return addFilter(propertyName, FilterOperator.GREATER_THAN, value);
	}
	
	@Override
	public SimpleQuery greaterThanOrEqual(String propertyName, Object value) {
		return addFilter(propertyName, FilterOperator.GREATER_THAN_OR_EQUAL, value);
	}
	
	@Override
	public SimpleQuery lessThan(String propertyName, Object value) {
		return addFilter(propertyName, FilterOperator.LESS_THAN, value);
	}
	
	@Override
	public SimpleQuery lessThanOrEqual(String propertyName, Object value) {
		return addFilter(propertyName, FilterOperator.LESS_THAN_OR_EQUAL, value);
	}
	
	@Override
	public SimpleQuery notEqual(String propertyName, Object value) {
		return addFilter(propertyName, FilterOperator.NOT_EQUAL, value);
	}
	
	@Override
	public SimpleQuery in(String propertyName, Collection<?> values) {
		if (values != null) {
			PropertyMetadata propertyMetadata = getPropertyMetadata(propertyName);
			List convertedValues = Lists.newArrayListWithCapacity(values.size());
			for (Object value : values) {
				convertedValues.add(propertyMetadata.convertQueryParam(value));
			}
			query.addFilter(propertyName, FilterOperator.IN, convertedValues);
		}
		return this;
	}

	@Override
	public SimpleQuery like(String propertyName, String value) {
		if (value != null) {
			this.greaterThanOrEqual(propertyName, value);
			this.lessThan(propertyName, value + '\ufffd');
		}
		return this;
	}
	
	@Override
	public SimpleQuery sortAsc(String propertyName) {
		return sort(propertyName, SortDirection.ASCENDING);
	}
	
	/**
	 * @deprecated use sortAsc instead
	 */
	@Deprecated
	@Override
	public SimpleQuery orderAsc(String propertyName) {
		return sortAsc(propertyName);
	}
	
	@Override
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
	@Override
	public SimpleQuery orderDesc(String propertyName) {
		return sortDesc(propertyName);
	}
	
	@Override
	public SimpleQuery sortDesc(String propertyName) {
		return sort(propertyName, SortDirection.DESCENDING);
	}
	
	@Override
	public SimpleQuery keysOnly() {
		query.setKeysOnly();
		return this;
	}
	
	public SimpleQuery withLimit(int limit) {
		fetchOptions = fetchOptions == null? FetchOptions.Builder.withLimit(limit) : fetchOptions.limit(limit);
		return this;
	}
	
	public SimpleQuery withOffset(int offset) {
		fetchOptions = fetchOptions == null? FetchOptions.Builder.withOffset(offset) : fetchOptions.offset(offset);
		return this;
	}
	
	@Override
	public SimpleQuery withPrefetchSize(int size) {
		fetchOptions = fetchOptions == null? FetchOptions.Builder.withPrefetchSize(size) : fetchOptions.prefetchSize(size);
		return this;
	}
	
	@Override
	public SimpleQuery withChunkSize(int size) {
		fetchOptions = fetchOptions == null? FetchOptions.Builder.withChunkSize(size) : fetchOptions.chunkSize(size);
		return this;
	}
	
	@Override
	public SimpleQuery withCursor(Cursor cursor) {
		if (cursor != null) {
			fetchOptions = fetchOptions == null? FetchOptions.Builder.withCursor(cursor) : fetchOptions.cursor(cursor);
		}
		return this;
	}
	
	@Override
	public SimpleQuery withFetchOptions(FetchOptions fetchOptions) {
		this.fetchOptions = fetchOptions;
		return this;
	}
	
	@Override
	public SimpleQuery withCursor(String cursor) {
		return withCursor(cursor == null? null : Cursor.fromWebSafeString(cursor));
	}
	
	@Override
	public SimpleQuery withTransaction(Transaction transaction) {
		this.transaction = transaction;
		return this;
	}

	@Override
	public List<FilterPredicate> getFilterPredicates() {
		return query.getFilterPredicates();
	}

	@Override
	public List<SortPredicate> getSortPredicates() {
		return query.getSortPredicates();
	}
	
	@Override
	public boolean isKeysOnly() {
		return query.isKeysOnly();
	}
	
	public Query getQuery() {
		return query;
	}

	@Override
	public FetchOptions getFetchOptions() {
		return fetchOptions;
	}

	@Override
	public ClassMetadata getClassMetadata() {
		return classMetadata;
	}

	@Override
	public Transaction getTransaction() {
		return transaction;
	}

	/** 
	 * Execute the provided query and returns the result as a List of java objects 
	 * @param query the query to execute
	 * @return the list of resulting java entities
	 */
	public <T> List<T> asList() {
		if (cacheKey != null && transaction == null) {
			List<Key> keys = getCacheManager().get(cacheKey);
			if (keys != null) {
				return isKeysOnly()? (List) keys : entityManager.get(keys);
			}
		}
		
		List<T> result = Lists.newArrayList();
		SimpleQueryResultIterable<T> iterable = asIterable();
		for (T item : iterable) {
			result.add(item);
		}
		
		if (cacheKey != null) {
			Collection<Key> keys = isKeysOnly()? result : Collections2.transform(result, new EntityToKeyFunction(classMetadata.getPersistentClass()));
			getCacheManager().put(cacheKey, Lists.newArrayList(keys), cacheSeconds);
		}
		return result;
	}
	
	@Override
	public void clearCache() {
		if (cacheKey == null) {
			throw new IllegalStateException();
		}
		getCacheManager().delete(ImmutableList.of(cacheKey, cacheKey + CacheManager.COUNT_SUFFIX));
	}
	
	/**
	 * Execute the query and return a single result
	 * @return the first result of the query
	 * @throws EntityNotFoundException if the query did not return any result
	 */
	public <T> T asSingleResult() {
		T javaObject = null;
		if (cacheKey != null && transaction == null) {
			Collection<Key> keys = getCacheManager().get(cacheKey);
			if (keys != null && keys.size() > 0) {
				javaObject = entityManager.get(keys.iterator().next());
			}
		}
		if (javaObject == null) {
			Entity entity = getDatastoreService().prepare(query).asSingleEntity();
			if (entity == null) {
				throw new org.simpleds.exception.EntityNotFoundException();
			}
			javaObject = (T) entityManager.datastoreToJava(entity);
			if (cacheKey != null) {
				getCacheManager().put(cacheKey, ImmutableList.of(entity.getKey()), cacheSeconds);
			}
		}
		return javaObject;

	}

	/**
	 * Counts the number of instances returned from this query. This method will only
	 * retrieve the matching keys, not the entities themselves.
	 */
	public int count() {
		Integer result = null;
		if (cacheKey != null && transaction == null) {
			result = getCacheManager().get(cacheKey + CacheManager.COUNT_SUFFIX);
		}
		if (result == null) {
			SimpleQuery q = this.isKeysOnly()? this : this.clone().keysOnly();
			result = getDatastoreService().prepare(q.getQuery()).countEntities();
			if (cacheKey != null) {
				getCacheManager().put(cacheKey + CacheManager.COUNT_SUFFIX, result, cacheSeconds);
			}
		}
		return result;
	}


	/** 
	 * Execute this query and return the result as a {@link SimpleQueryResultIterable} of java objects.
	 * This method does not check the cache.
	 * @return the list of resulting java entities
	 */
	public <T> SimpleQueryResultIterable<T> asIterable() {
		PreparedQuery preparedQuery = getDatastoreService().prepare(transaction, query);
		QueryResultIterable<Entity> iterable = fetchOptions == null? preparedQuery.asQueryResultIterable() : preparedQuery.asQueryResultIterable(fetchOptions);
		return new SimpleQueryResultIterableImpl<T>(classMetadata, iterable).setKeysOnly(isKeysOnly());
	}
	
	/** 
	 * Execute this query and returns the result as a {@link SimpleQueryResultIterator} of java objects
	 * This method does not check the cache.
	 * @return the list of resulting java entities
	 */
	public <T> SimpleQueryResultIterator<T> asIterator() {
		SimpleQueryResultIterable<T> iterable = asIterable();
		return iterable.iterator();
	}

	@Override
	public SimpleQuery withDeadline(double deadline) {
		if (datastoreServiceConfig == null) {
			datastoreServiceConfig = DatastoreServiceConfig.Builder.withDefaults();
		}
		datastoreServiceConfig = datastoreServiceConfig.deadline(deadline);
		return this;
	}

	@Override
	public SimpleQuery withReadPolicy(ReadPolicy readPolicy) {
		if (datastoreServiceConfig == null) {
			datastoreServiceConfig = DatastoreServiceConfig.Builder.withDefaults();
		}
		datastoreServiceConfig = datastoreServiceConfig.readPolicy(readPolicy);
		return this;
	}
	
	private DatastoreService getDatastoreService() {
		return datastoreServiceConfig == null? entityManager.getDatastoreService() : 
			DatastoreServiceFactory.getDatastoreService(datastoreServiceConfig);
	}

	@Override
	public SimpleQuery withCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		return this;
	}

	@Override
	public SimpleQuery withCacheSeconds(int cacheSeconds) {
		this.cacheSeconds = cacheSeconds;
		return this;
	}
	
	private CacheManager getCacheManager() {
		return entityManager.getCacheManager();
	}
	
}
