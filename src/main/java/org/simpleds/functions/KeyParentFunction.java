package org.simpleds.functions;

import com.google.appengine.api.datastore.Key;
import com.google.common.base.Function;

/**
 * Converts a list of Key into its parent Key 
 * @author icoloma
 *
 */
public class KeyParentFunction implements Function<Key, Key> {
	
	@Override
	public Key apply(Key key) {
		return key.getParent();
	}
	
}