package org.simpleds.functions;

import com.google.appengine.api.datastore.Key;
import com.google.common.base.Function;

/**
 * Transforms a Key into its id value 
 * @author icoloma
 *
 */
public class KeyToIdFunction implements Function<Key, Long> {

	@Override
	public Long apply(Key from) {
		return from == null? null : from.getId();
	}
	
}
