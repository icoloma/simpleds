package org.simpleds.cache;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Configure this filter in web.xml to get Level 1 caching. 
 * 
 * <pre>
&lt;filter>
	&lt;filter-name>simpleds-cache&lt;/filter-name>
	&lt;filter-class>org.simpleds.cache.CacheFilter&lt;/filter-class>
&lt;/filter>
&lt;filter-mapping>
	&lt;filter-name>simpleds-cache&lt;/filter-name>
	&lt;url-pattern>/*&lt;/url-pattern>
	&lt;dispatcher>REQUEST&lt;/dispatcher>
&lt;/filter-mapping>
 * </pre>
 * 
 * @author Nacho
 *
 */
public class CacheFilter implements Filter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		try {
			Level1Cache.setCacheInstance();
			chain.doFilter(req, resp);
		} finally {
			Level1Cache.clearCacheInstance();
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	@Override
	public void destroy() {
	}
	
}
