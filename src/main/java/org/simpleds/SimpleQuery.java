package org.simpleds;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
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
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.SortPredicate;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;

/**
 * Proxy class to handle a {@link Query} instance. 
 * @author icoloma
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SimpleQuery implements ParameterQuery, Cloneable {
	
	/** the value of cacheSeconds to skip cache */
	static final int NO_CACHE = -1;

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
	
	/** the number of seconds to store data in the memcache. Default (0) will only use the Level 1 cache */
	private int cacheSeconds = NO_CACHE;
	
	/** Predicate to filter using Java code */
	private Predicate predicate;
	
	SimpleQuery(EntityManager entityManager, Key ancestor, ClassMetadata metadata) {
		this.entityManager = entityManager;
		this.classMetadata = metadata;
		this.query = new Query(metadata.getKind(), ancestor);
		this.fetchOptions = FetchOptions.Builder.withDefaults();
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
		if (fetchOptions.getStartCursor() != null) {
			copy.withStartCursor(fetchOptions.getStartCursor());
		}
		if (fetchOptions.getEndCursor() != null) {
			copy.withEndCursor(fetchOptions.getEndCursor());
		}
		copy.withTransaction(transaction);
		copy.datastoreServiceConfig = datastoreServiceConfig;
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
		fetchOptions.limit(limit);
		return this;
	}
	
	public SimpleQuery withOffset(int offset) {
		fetchOptions.offset(offset);
		return this;
	}
	
	@Override
	public SimpleQuery withPrefetchSize(int size) {
		fetchOptions.prefetchSize(size);
		return this;
	}
	
	@Override
	public SimpleQuery withChunkSize(int size) {
		fetchOptions.chunkSize(size);
		return this;
	}
	
	/**
	 * Set the {@link Cursor} to use with this query. This method is an alias of {@link SimpleQuery#withStartCursor(Cursor)}
	 * @param cursor the cursor to use with this query. If null, it will be ignored.
	 * @return this instance
	 */
	public SimpleQuery withCursor(Cursor cursor) {
		return withStartCursor(cursor);
	}
	
	/**
	 * Set the {@link Cursor} at which to start this query.
	 * @param cursor the cursor to use with this query. If null, it will be ignored.
	 * @return this instance
	 */
	public SimpleQuery withStartCursor(Cursor cursor) {
		if (cursor != null) {
			fetchOptions.startCursor(cursor);
		}
		return this;
	}
	
	/**
	 * Set the {@link Cursor} at which to end this query.
	 * @param cursor the cursor to use with this query. If null, it will be ignored.
	 * @return this instance
	 */
	public SimpleQuery withEndCursor(Cursor cursor) {
		if (cursor != null) {
			fetchOptions.endCursor(cursor);
		}
		return this;
	}
	
	@Override
	public SimpleQuery withFetchOptions(FetchOptions fetchOptions) {
		this.fetchOptions = fetchOptions;
		return this;
	}
	

	/**
	 * Set the serialized {@link Cursor} to use with this query.
	 * This method is equivalent to invoking 
	 * withCursor(Cursor.fromWebsafeString(cursor)), but it also accepts null values
	 * @param cursor the web-safe serialized cursor value. If null, it will be ignored.
	 * @return this instance
	 * @deprecated use withStartCursor(String) instead
	 */
	@Deprecated
	public SimpleQuery withCursor(String cursor) {
		return withStartCursor(cursor);
	}
	
	/**
	 * Set the serialized {@link Cursor} to use with this query.
	 * This method is equivalent to invoking 
	 * withStartCursor(Cursor.fromWebsafeString(cursor)), but it also accepts null values
	 * @param cursor the web-safe serialized cursor value. If null, it will be ignored.
	 * @return this instance
	 */
	public SimpleQuery withStartCursor(String cursor) {
		return withStartCursor(cursor == null? null : Cursor.fromWebSafeString(cursor));
	}
	
	/**
	 * Set the serialized {@link Cursor} to use with this query.
	 * This method is equivalent to invoking 
	 * withEndCursor(Cursor.fromWebsafeString(cursor)), but it also accepts null values
	 * @param cursor the web-safe serialized cursor value. If null, it will be ignored.
	 * @return this instance
	 */
	public SimpleQuery withEndCursor(String cursor) {
		return withEndCursor(cursor == null? null : Cursor.fromWebSafeString(cursor));
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
	 * @return the list of resulting java entities
	 */
	public <T> List<T> asList() {
		String cacheKey = null;
		
		// is the result of the query cached?
		if (isCacheable() && transaction == null) {
			cacheKey = calculateDataCacheKey();
			List<Key> keys = getCacheManager().get(cacheKey);
			if (keys != null) {
				if (isKeysOnly()) {
					return (List) keys;
				} else {
					final Map<Key, T> values = entityManager.get(keys);
					return Lists.transform(keys, new Function<Key, T>() {

						@Override
						public T apply(Key key) {
							return values.get(key);
						}
						
					});
				}
			}
		}
		
		// execute the query
		List<T> result = Lists.newArrayList((Iterable<T>) asIterable());
		if (isCacheable()) {
			Collection<Key> keys = isKeysOnly()? result : Collections2.transform(result, new EntityToKeyFunction(classMetadata.getPersistentClass()));
			populateCache(cacheKey, Lists.newArrayList(keys));
		}
		return result;
	}
	
	@Override
	public void clearCache() {
		getCacheManager().delete(ImmutableList.of(calculateDataCacheKey()));
	}
	
	private boolean isCacheable() {
		return cacheSeconds != NO_CACHE;
	}
	
	/**
	 * Execute the query and return a CursorList
	 * @return the CursorList according to the provided startCursor and limit values
	 */
	public <T> CursorList<T> asCursorList(int size) {
		CursorList<T> result = CursorList.create(this, size);
		return result;
	}
	
	/**
	 * Execute the query and return a single result
	 * @return the first result of the query
	 * @throws EntityNotFoundException if the query did not return any result
	 */
	public <T> T asSingleResult() {
		T javaObject = null;
		String cacheKey = calculateDataCacheKey();
		if (isCacheable() && transaction == null) {
			Collection<Key> keys = getCacheManager().get(cacheKey);
			if (keys != null && keys.size() > 0) {
				javaObject = (T) entityManager.get(keys.iterator().next());
			}
		}
		if (javaObject == null) {
			Entity entity = getDatastoreService().prepare(query).asSingleEntity();
			if (entity == null) {
				throw new org.simpleds.exception.EntityNotFoundException("No " + getKind() + " found with " + getFilterPredicates());
			}
			javaObject = (T) entityManager.datastoreToJava(entity);
			if (isCacheable()) {
                populateCache(cacheKey, ImmutableList.of(entity.getKey()));
			}
		}
		return javaObject;

	}

    private void populateCache(String cacheKey, List<Key> elements) {
        getCacheManager().put(cacheKey, elements, cacheSeconds);
    }

    @Override
    public void populateCache(List<Key> keys) {
        Preconditions.checkState(isCacheable(), "Query is not cacheable. Invoke withCacheSeconds() first");
        populateCache(calculateDataCacheKey(), keys);
    }

	/**
	 * Counts the number of instances returned from this query. This method will only
	 * retrieve the matching keys, not the entities themselves.
	 */
	public int count() {
		Integer result = null;
		if (result == null) {
			SimpleQuery q = this.isKeysOnly()? this : this.clone().keysOnly();
			result = getDatastoreService().prepare(q.getQuery()).countEntities(fetchOptions);
		}
		return result;
	}


	/** 
	 * Execute this query and return the result as a {@link CursorIterable} of java objects.
	 * This method does not check the cache.
	 * @return the list of resulting java entities
	 */
	public <T> CursorIterable<T> asIterable() {
		PreparedQuery preparedQuery = getDatastoreService().prepare(transaction, query);
		QueryResultIterable<Entity> iterable = preparedQuery.asQueryResultIterable(fetchOptions);
		return new CursorIterableImpl<T>(classMetadata, predicate, iterable).setKeysOnly(isKeysOnly());
	}
	
	/** 
	 * Execute this query and returns the result as a {@link CursorIterator} of java objects.
	 * This method does not check the cache.
	 * @return the list of resulting java entities
	 */
	public <T> CursorIterator<T> asIterator() {
		CursorIterable<T> iterable = asIterable();
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

	/** 
	 * Calculate the cache key to use for query data. This method combines the query kind, any filter predicates,
 	 * the start/end cursors and limit / offset values to produce a cache key
	 */
	protected String calculateDataCacheKey() {
		StringBuilder builder = new StringBuilder(100);
		builder.append("qdata{");
		addCommonCacheKeyParts(builder);
		if(fetchOptions.getOffset() != null) {
			builder.append(",off=").append(fetchOptions.getOffset());
		}
		if(fetchOptions.getLimit() != null) {
			builder.append(",lim=").append(fetchOptions.getLimit());
		}
		if (fetchOptions.getStartCursor() != null) {
			builder.append(",start=").append(fetchOptions.getStartCursor().toWebSafeString());
		}
		if (fetchOptions.getEndCursor() != null) {
			builder.append(",end=").append(fetchOptions.getEndCursor().toWebSafeString());
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Add cache key parts that are common to query data and query count.
	 * @param builder
	 */
	private void addCommonCacheKeyParts(StringBuilder builder) {
		builder.append("kind=").append(getKind());
		List<FilterPredicate> predicates = query.getFilterPredicates();
		if (predicates.size() > 0) {
			builder.append(",pred=").append(predicates);
		}
	}
	
	@Override
	public SimpleQuery withCacheSeconds(int cacheSeconds) {
		this.cacheSeconds = cacheSeconds;
		return this;
	}
	
	private CacheManager getCacheManager() {
		return entityManager.getCacheManager();
	}
	
	@Override
	public SimpleQuery withPredicate(Predicate<?> predicate) {
		this.predicate = predicate;
		return this;
	}

	public Predicate<?> getPredicate() {
		return predicate;
	}
	
}
