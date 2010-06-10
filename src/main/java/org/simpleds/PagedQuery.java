package org.simpleds;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.simpleds.metadata.ClassMetadata;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.SortPredicate;

/**
 * Paged query.
 * This is a simple class to implement paged queries using FetchOptions. 
 * These queries are limited to a maximum of 1000 rows.
 * <p>
 * PagedQuery.getFetchOptions() will return a FetchOptions instance that 
 * corresponds to the current page.
 * <p>  
 * 
 * Paging has been explained <a href="http://code.google.com/appengine/articles/paging.html">here</a>
 * 
 * @author icoloma
 *
 */
public class PagedQuery implements ParameterQuery, Cloneable {

	/** the default size of each page */
	public static final int DEFAULT_PAGE_SIZE = 25;
	
	/** the size of each page */
	private int pageSize = DEFAULT_PAGE_SIZE;
	
	/** the index of the current page, starts at 0 */
	private int pageIndex;
	
	/** true to calculate the total size of the response, defaults to true */
	private boolean calculateTotalResults = true;
	
	private SimpleQuery query;
	
	PagedQuery(EntityManager entityManager, Key ancestor, ClassMetadata metadata) {
		query = new SimpleQuery(entityManager, ancestor, metadata);
	}
	
	@Override
	public FetchOptions getFetchOptions() {
		FetchOptions fo = query.getFetchOptions();
		fo = fo == null? FetchOptions.Builder.withLimit(pageSize) : fo.limit(pageSize); 
		return fo.offset(getFirstRecordIndex());
	}
	
	/**
	 * @return the zero-based index of the first record that is returned. Internally, it multiplies the page index and the page size.
	 */
	public int getFirstRecordIndex() {
		return pageIndex * pageSize;
	}

	@Override
	public PagedQuery addFilter(String propertyName, FilterOperator operator, Object value) {
		query.addFilter(propertyName, operator, value);
		return this;
	}
	
	@Override
	public PagedQuery isNull(String propertyName) {
		query.isNull(propertyName);
		return this;
	}
	
	@Override
	public PagedQuery isNotNull(String propertyName) {
		query.isNotNull(propertyName);
		return this;
	}

	@Override
	public PagedQuery like(String propertyName, String value) {
		query.like(propertyName, value);
		return this;
	}
	
	@Override
	public PagedQuery notEqual(String propertyName, Object value) {
		query.notEqual(propertyName, value);
		return this;
	}
	
	@Override
	public PagedQuery in(String propertyName, Collection<?> values) {
		query.in(propertyName, values);
		return this;
	}
	
	@Override
	public PagedQuery equal(String propertyName, Object value) {
		query.equal(propertyName, value);
		return this;
	}
	
	@Override
	public PagedQuery lessThan(String propertyName, Object value) {
		query.lessThan(propertyName, value);
		return this;
	}
	
	@Override
	public PagedQuery lessThanOrEqual(String propertyName, Object value) {
		query.lessThanOrEqual(propertyName, value);
		return this;
	}
	
	@Override
	public PagedQuery greaterThan(String propertyName, Object value) {
		query.greaterThan(propertyName, value);
		return this;
	}
	
	@Override
	public PagedQuery greaterThanOrEqual(String propertyName, Object value) {
		query.greaterThanOrEqual(propertyName, value);
		return this;
	}
	
	@Override
	public PagedQuery keysOnly() {
		query.keysOnly();
		return this;
	}
	
	@Override
	public PagedQuery orderAsc(String propertyName) {
		query.sortAsc(propertyName);
		return this;
	}
	
	@Override
	public PagedQuery sortAsc(String propertyName) {
		query.sortAsc(propertyName);
		return this;
	}
	
	@Override
	public PagedQuery sort(String propertyName, SortDirection direction) {
		query.sort(propertyName, direction);
		return this;
	}
	
	@Override
	public PagedQuery sortDesc(String propertyName) {
		query.sortDesc(propertyName);
		return this;
	}
	
	@Override
	public PagedQuery orderDesc(String propertyName) {
		query.sortDesc(propertyName);
		return this;
	}
	
	@Override
	public PagedQuery withChunkSize(int size) {
		query.withChunkSize(size);
		return this;
	}
	
	@Override
	public PagedQuery withLimit(int limit) {
		query.withLimit(limit);
		return this;
	}
	
	@Override
	public PagedQuery withOffset(int offset) {
		query.withOffset(offset);
		return this;
	}
	
	@Override
	public PagedQuery withPrefetchSize(int size) {
		query.withPrefetchSize(size);
		return this;
	}
	
	@Override
	public PagedQuery withCursor(Cursor cursor) {
		query.withCursor(cursor);
		return this;
	}
	
	@Override
	public PagedQuery withCursor(String cursor) {
		query.withCursor(cursor);
		return this;
	}
	
	@Override
	public PagedQuery withFetchOptions(FetchOptions fetchOptions) {
		query.withFetchOptions(fetchOptions);
		return this;
	}
	
	@Override
	public PagedQuery withTransaction(Transaction transaction) {
		query.withTransaction(transaction);
		return this;
	}
	
	public PagedQuery setPageSize(int pageSize) {
		this.pageSize = pageSize;
		return this;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public PagedQuery setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
		return this;
	}
	
	public int getPageSize() {
		return pageSize;
	}

	public boolean calculateTotalResults() {
		return calculateTotalResults;
	}

	public PagedQuery setCalculateTotalResults(boolean calculateTotalResults) {
		this.calculateTotalResults = calculateTotalResults;
		return this;
	}

	@Override
	public ClassMetadata getClassMetadata() {
		return query.getClassMetadata();
	}

	@Override
	public List<FilterPredicate> getFilterPredicates() {
		return query.getFilterPredicates();
	}

	@Override
	public String getKind() {
		return query.getKind();
	}

	@Override
	public List<SortPredicate> getSortPredicates() {
		return query.getSortPredicates();
	}

	@Override
	public Transaction getTransaction() {
		return query.getTransaction();
	}

	@Override
	public boolean isKeysOnly() {
		return query.isKeysOnly();
	}
	
	/**
	 * Return a {@link PagedList} result after computing this query
	 * @return the result of the query
	 */
	public <T> PagedList<T> find() {
		int totalResults = -1;
		if (calculateTotalResults) {
			totalResults = query.count();
		}
		List<T> data = totalResults == 0? new ArrayList<T>() : (List<T>) query.find();
		PagedList pagedList = new PagedList<T>(this, data);
		pagedList.setTotalResults(totalResults);
		return pagedList;
	}

	@Override
	public ParameterQuery withDeadline(double deadline) {
		query.withDeadline(deadline);
		return this;
	}

	@Override
	public ParameterQuery withReadPolicy(ReadPolicy readPolicy) {
		query.withReadPolicy(readPolicy);
		return this;
	}
}
