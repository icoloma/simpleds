package org.simpleds.functions;

import com.google.appengine.api.datastore.Key;
import com.google.common.base.Function;

/**
 * Converts a Key into its parent Key 
 * @author icoloma
 *
 */
public class KeyToParentKeyFunction implements Function<Key, Key> {
	
	@Override
	public Key apply(Key key) {
		return key.getParent();
	}
	
}