package org.simpleds;

import java.util.Collection;
import java.util.List;

import org.simpleds.metadata.ClassMetadata;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.SortPredicate;

/**
 * The parameter methods associated to a query
 * @author icoloma
 */
public interface ParameterQuery {

	/**
	 * @return the kind of this query
	 */
	public String getKind();

	/**
	 * Insert an equal (=) comparison. 
	 * @param propertyName the name of the property 
	 * @param value the value to compare. If null, this method does nothing and returns.
	 * @return this instance
	 */
	public ParameterQuery equal(String propertyName, Object value);

	/**
	 * Insert a not equal (!=) comparison. 
	 * @param propertyName the name of the property 
	 * @param value the value to compare. If null, this method does nothing and returns.
	 * @return this instance
	 */
	public ParameterQuery notEqual(String propertyName, Object value);

	/**
	 * Insert a IS NULL comparison. 
	 * @param propertyName the name of the property 
	 * @return this instance
	 */
	public ParameterQuery isNull(String propertyName);

	/**
	 * Insert a IS NOT NULL comparison. 
	 * @param propertyName the name of the property 
	 * @return this instance
	 */
	public ParameterQuery isNotNull(String propertyName);

	/**
	 * Insert a strict greater (>) comparison. 
	 * @param propertyName the name of the property 
	 * @param value the value to compare. If null, this method does nothing and returns.
	 * @return this instance
	 */
	public ParameterQuery greaterThan(String propertyName, Object value);

	/**
	 * Insert a greater or equal (>=) comparison. 
	 * @param propertyName the name of the property 
	 * @param value the value to compare. If null, this method does nothing and returns.
	 * @return this instance
	 */
	public ParameterQuery greaterThanOrEqual(String propertyName, Object value);

	/**
	 * Insert a strict less (<) comparison. 
	 * @param propertyName the name of the property 
	 * @param value the value to compare. If null, this method does nothing and returns.
	 * @return this instance
	 */
	public ParameterQuery lessThan(String propertyName, Object value);

	/**
	 * Insert a less or equal (<=) comparison. 
	 * @param propertyName the name of the property 
	 * @param value the value to compare. If null, this method does nothing and returns.
	 * @return this instance
	 */
	public ParameterQuery lessThanOrEqual(String propertyName, Object value);

	/**
	 * Insert an IN comparison. 
	 * @param propertyName the name of the property 
	 * @param values the list of values to compare. If null, this method does nothing and returns.
	 * @return this instance
	 */
	public ParameterQuery in(String propertyName, Collection<?> values);

	/**
	 * Insert a LIKE comparison. This clause will only match strings that start
	 * with the provided value. In other words, this clause will match "foo%" 
	 * but not "%foo%"
	 * @param propertyName the name of the property
	 * @param value the value of the property, without any '%' character.
	 * @return this instance
	 */
	public ParameterQuery like(String propertyName, String value);

	/**
	 * Specify an ascending order.
	 * @param propertyName the property to sort by.
	 * @return this instance
	 */
	public ParameterQuery sortAsc(String propertyName);
	
	/**
	 * Specify a descending order.
	 * @param propertyName the property to sort by.
	 * @return this instance
	 */
	public ParameterQuery sortDesc(String propertyName);
	
	/**
	 * Specify an ascending or descending order.
	 * @param propertyName the property to sort by.
	 * @return this instance
	 */
	public ParameterQuery sort(String propertyName, SortDirection direction);

	/**
	 * @deprecated use {@link ParameterQuery#sortAsc()} instead
	 */
	@Deprecated
	public ParameterQuery orderAsc(String propertyName);

	/**
	 * @deprecated use {@link ParameterQuery#sortDesc()} instead
	 */
	@Deprecated
	public ParameterQuery orderDesc(String propertyName);

	/**
	 * Set this query to retrieve and return {@link Key} instances only.
	 * This yields better performance for the cases where only the {@link Key} values
	 * are required.
	 * @return this instance
	 */
	public ParameterQuery keysOnly();

	/**
	 * @return true if the keysOnly method has been invoked for this instance.
	 */
	public boolean isKeysOnly();

	public ParameterQuery withPrefetchSize(int size);

	public ParameterQuery withChunkSize(int size);

	public ParameterQuery withCursor(Cursor cursor);

	public ParameterQuery withFetchOptions(FetchOptions fetchOptions);

	/**
	 * Set the serialized {@link Cursor} to use with this query.
	 * This method is equivalent to invoking 
	 * withCursor(Cursor.fromWebsafeString(cursor))
	 * @param cursor the web-safe serialized cursor value
	 * @return this instance
	 */
	public ParameterQuery withCursor(String cursor);

	/**
	 * Set the transaction to use with this query
	 * @param transaction the transaction to use (can be null)
	 * @return this instance
	 */
	public ParameterQuery withTransaction(Transaction transaction);

	/**
	 * Set the {@link ReadPolicy} instance to use with this query.
	 * @param readPolicy the {@link ReadPolicy} to use.
	 * @return this instance
	 * @see DatastoreServiceConfig.Builder#withReadPolicy(ReadPolicy)
	 */
	public ParameterQuery withReadPolicy(ReadPolicy readPolicy);

	/**
	 * Set the deadline to use for this query. The default is 30 seconds.
	 * @param deadline the deadline to apply
	 * @return this instance
	 * @see DatastoreServiceConfig.Builder#withDeadline(double)
	 */
	public ParameterQuery withDeadline(double deadline);
	
	/**
	 * @return the value of the transaction applied with this query, if any
	 */
	public Transaction getTransaction();

	/**
	 * Insert a {@link FilterOperator} directly into the underlying {@link Query} instance. 
	 * Its use is discouraged in the majority of cases.
	 * @param propertyName the name of the property 
	 * @param value the value to compare. 
	 * @return this instance
	 */
	public ParameterQuery addFilter(String propertyName, FilterOperator operator, Object value);

	/**
	 * @return the list of {@link FilterPredicate} instances assigned 
	 * to the underlying {@link Query} object
	 */
	public List<FilterPredicate> getFilterPredicates();

	/**
	 * @return the list of {@link SortPredicate} instances assigned 
	 * to the underlying {@link Query} object
	 */
	public List<SortPredicate> getSortPredicates();

	/**
	 * @return the FetchOptions instance assigned 
	 * to the underlying {@link Query} object
	 */
	public FetchOptions getFetchOptions();

	/**
	 * @return the {@link ClassMetadata} of the class returned by this query.
	 */
	public ClassMetadata getClassMetadata();

}
