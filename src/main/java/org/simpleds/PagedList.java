package org.simpleds;

import java.util.List;

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
}
