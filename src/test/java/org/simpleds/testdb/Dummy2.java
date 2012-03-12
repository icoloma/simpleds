package org.simpleds.testdb;

import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;

import com.google.appengine.api.datastore.Key;

@Entity("d2")
public class Dummy2 {

	@Id(generated=true, parent=Dummy1.class)
	private Key key;

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}
	
}
