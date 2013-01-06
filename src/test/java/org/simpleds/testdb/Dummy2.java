package org.simpleds.testdb;

import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;

import com.google.appengine.api.datastore.Key;
import org.simpleds.annotations.Property;

@Entity(Kinds.DUMMY2)
public class Dummy2 {

	@Id(generated=true, parent=Dummy1.class) @Property(Attrs.KEY)
	private Key key;

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}
	
}
