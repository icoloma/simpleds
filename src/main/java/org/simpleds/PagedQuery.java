package org.simpleds;


import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;

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
public class PagedQuery extends SimpleQuery {

	/** the default size of each page */
	public static final int DEFAULT_PAGE_SIZE = 25;
	
	/** the size of each page */
	private int pageSize = DEFAULT_PAGE_SIZE;
	
	/** the index of the current page, starts at 0 */
	private int pageIndex;
	
	/** true to calculate the total size of the response, defaults to true */
	private boolean calculateTotalResults = true;
	
	public PagedQuery(Class clazz) {
		super(clazz);
	}

	public PagedQuery(Key ancestor, Class clazz) {
		super(ancestor, clazz);
	}

	public PagedQuery(Key ancestor, String kind) {
		super(ancestor, kind);
	}

	public PagedQuery(Key ancestor) {
		super(ancestor);
	}

	public PagedQuery(String kind) {
		super(kind);
	}

	@Override
	public FetchOptions getFetchOptions() {
		FetchOptions fo = super.getFetchOptions();
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
		return (PagedQuery) super.addFilter(propertyName, operator, value);
	}
	
	@Override
	public PagedQuery isNull(String propertyName) {
		return (PagedQuery) super.isNull(propertyName);
	}
	
	@Override
	public PagedQuery isNotNull(String propertyName) {
		return (PagedQuery) super.isNotNull(propertyName);
	}

	@Override
	public PagedQuery like(String propertyName, String value) {
		return (PagedQuery) super.like(propertyName, value);
	}
	
	@Override
	public PagedQuery equal(String propertyName, Object value) {
		return (PagedQuery) super.equal(propertyName, value);
	}
	
	@Override
	public PagedQuery lessThan(String propertyName, Object value) {
		return (PagedQuery) super.lessThan(propertyName, value);
	}
	
	@Override
	public PagedQuery lessThanOrEqual(String propertyName, Object value) {
		return (PagedQuery) super.lessThanOrEqual(propertyName, value);
	}
	
	@Override
	public PagedQuery greaterThan(String propertyName, Object value) {
		return (PagedQuery) super.greaterThan(propertyName, value);
	}
	
	@Override
	public PagedQuery greaterThanOrEqual(String propertyName, Object value) {
		return (PagedQuery) super.greaterThanOrEqual(propertyName, value);
	}
	
	@Override
	public PagedQuery keysOnly() {
		return (PagedQuery) super.keysOnly();
	}
	
	@Override
	public PagedQuery orderAsc(String propertyName) {
		return (PagedQuery) super.orderAsc(propertyName);
	}
	
	@Override
	public PagedQuery order(String propertyName, SortDirection direction) {
		return (PagedQuery) super.order(propertyName, direction);
	}
	
	@Override
	public PagedQuery orderDesc(String propertyName) {
		return (PagedQuery) super.orderDesc(propertyName);
	}
	
	@Override
	public PagedQuery withChunkSize(int size) {
		return (PagedQuery) super.withChunkSize(size);
	}
	
	@Override
	public PagedQuery withLimit(int limit) {
		return (PagedQuery) super.withLimit(limit);
	}
	
	@Override
	public PagedQuery withOffset(int offset) {
		return (PagedQuery) super.withOffset(offset);
	}
	
	@Override
	public PagedQuery withPrefetchSize(int size) {
		return (PagedQuery) super.withPrefetchSize(size);
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
}
