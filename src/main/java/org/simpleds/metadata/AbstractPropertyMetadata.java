package org.simpleds.metadata;

import org.simpleds.converter.Converter;

import com.google.appengine.api.datastore.Entity;


public abstract class AbstractPropertyMetadata<J, D> implements PropertyMetadata<J, D>{

	public void setEntityValue(Entity entity, J javaValue) {
		Converter converter = getConverter();
		Object dsValue = converter.javaToDatastore(javaValue);
		if (isIndexed()) {
			entity.setProperty(getName(), dsValue);
		} else {
			entity.setUnindexedProperty(getName(), dsValue);
		}
	}
	
}
