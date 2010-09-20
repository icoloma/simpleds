package org.simpleds.converter;

import java.util.SortedSet;
import java.util.TreeSet;

public class SortedSetConverter extends AbstractCollectionConverter<SortedSet> {

	protected SortedSetConverter() {
		super(SortedSet.class);
	}

	@Override
	public SortedSet createCollection(int size) {
		return new TreeSet();
	}

}
