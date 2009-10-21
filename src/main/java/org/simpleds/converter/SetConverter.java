package org.simpleds.converter;

import java.util.HashSet;
import java.util.Set;

public class SetConverter extends AbstractCollectionConverter<Set> {

	@Override
	public Set createCollection(int size) {
		return new HashSet(size);
	}

}
