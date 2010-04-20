package org.simpleds.cache;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Configure this filter in web.xml to get caching in all SimpleDS
 * operations.
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
