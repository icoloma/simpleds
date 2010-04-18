package org.simpleds.cache;

import java.util.Set;

import com.google.appengine.api.datastore.Key;
import com.google.common.base.Predicate;

/**
 * Filter a collection of Key, leaving only the ones that 
 * have not yet been retrieved from the cache
 * 
 * @author Nacho
 *
 */
public class NonCachedPredicate implements Predicate<Key> {
	
	private Set<Key> cachedKeys;
	
	public NonCachedPredicate(Set<Key> cachedKeys) {
		this.cachedKeys = cachedKeys;
	}

	@Override
	public boolean apply(Key key) {
		return !cachedKeys.contains(key);
	}

}
