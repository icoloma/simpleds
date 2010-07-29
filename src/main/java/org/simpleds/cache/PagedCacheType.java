package org.simpleds.cache;

import org.simpleds.PagedQuery;

/**
 * The type of caching to apply to a {@link PagedQuery}
 * @author icoloma
 *
 */
public enum PagedCacheType {

	/** Cache the current page of data, but not the total */ 
	DATA, 
	
	/** Cache the total number of records for this query, but not the page data */
	TOTAL, 
	
	/** Cache both the data and the total number of rows */
	BOTH
	
}
