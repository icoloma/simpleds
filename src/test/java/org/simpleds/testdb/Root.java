package org.simpleds.testdb;

import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;

import com.google.appengine.api.datastore.Key;
import org.simpleds.annotations.Property;

@Entity(Kinds.ROOT)
public class Root {

	@Id @Property(Attrs.KEY)
	private Key key;

	public Key getKey() {
		return key;
	}

	public void setKey(Key id) {
		this.key = id;
	}
	
}
