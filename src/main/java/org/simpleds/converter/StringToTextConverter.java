package org.simpleds.converter;

import com.google.appengine.api.datastore.Text;

public class StringToTextConverter implements Converter<String, Text> {

	@Override
	public String getNullValue() {
		return null;
	}
	
	@Override
	public String datastoreToJava(Text value) {
		return value == null? null : value.getValue();
	}

	@Override
	public Text javaToDatastore(String value) {
		return value == null? null : new Text(value);
	}

}
