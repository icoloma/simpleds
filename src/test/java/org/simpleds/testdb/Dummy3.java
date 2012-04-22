package org.simpleds.testdb;

import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;

import com.google.appengine.api.datastore.Key;

@Entity
public class Dummy3 {

	@Id(generated=false)
	private Key key;

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}
	
}
