package org.simpleds.testdb;

import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;

import com.google.appengine.api.datastore.Key;

@Entity
public class Root {

	@Id
	private Key key;

	public Key getKey() {
		return key;
	}

	public void setKey(Key id) {
		this.key = id;
	}
	
}
