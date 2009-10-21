package org.simpleds.converter;

import java.util.ArrayList;
import java.util.List;

public class ListConverter extends AbstractCollectionConverter<List> {

	@Override
	public List createCollection(int size) {
		return new ArrayList(size);
	}

}
