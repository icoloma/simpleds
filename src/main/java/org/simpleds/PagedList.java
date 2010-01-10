package org.simpleds;

import java.util.List;

import com.google.appengine.api.datastore.Query.SortPredicate;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * The result of executing a paged query
 * @author icoloma
 */
public class PagedList<T> {

	/** the executed PagedQuery */
	private PagedQuery query;
	
	/** the list of results in the current page */
	private List<T> data;
	
	/** the total number of results for the query, -1 if not initialized */
	private int totalResults = -1;
	
	/** the total number of pages, -1 if not initialized */
	private int totalPages = -1;

	public PagedList(PagedQuery query, List<T> data) {
		this.query = query;
		this.data = data;
	}
	
	public PagedList<T> setTotalResults(int totalResults) {
		this.totalResults = totalResults;
		this.totalPages = (int) Math.ceil((double)totalResults / query.getPageSize());
		return this;
	}
	
	/**
	 * Transform this PagedList instance by applying the transformation function to each data list item
	 * @param <O> the type of the resulting PagedList, after applying the transformation
	 * @param function the function to apply to each data item
	 * @return a new PagedList instance. The original instance is not modified.
	 */
	public <O> PagedList<O> transform(Function<? super T, ? extends O> function) {
		PagedList<O> copy = new PagedList<O>(query, Lists.transform(this.data, function));
		copy.setTotalResults(totalResults);
		return copy;
	}

	
	/**
	 * @return the zero-based index of the first record on the next page. 
	 */
	public int getNextPageRecordIndex() {
		int last = query.getFirstRecordIndex() + query.getPageSize();
		return Math.min(last, totalResults);
	}

	/**
	 * @return true if the list of returned data is empty
	 */
	public boolean isEmpty() {
		return data.isEmpty();
	}
	
	/**
	 * @return the zero-based index of the first record that is returned. Internally, it multiplies the page index and the page size.
	 */
	public int getFirstRecordIndex() {
		return query.getFirstRecordIndex();
	}

	public List<T> getData() {
		return data;
	}
	
	public PagedList<T> setData(List<T> data) {
		this.data = data;
		return this;
	}
	
	public int getTotalResults() {
		return totalResults;
	}
	
	public int getTotalPages() {
		return totalPages;
	}

	public int getPageIndex() {
		return query.getPageIndex();
	}

	public int getPageSize() {
		return query.getPageSize();
	}

	public List<SortPredicate> getSortPredicates() {
		return query.getSortPredicates();
	}	
}
