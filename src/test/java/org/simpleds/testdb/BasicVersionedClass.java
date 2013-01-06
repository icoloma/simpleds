package org.simpleds.testdb;

import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;
import org.simpleds.annotations.Property;
import org.simpleds.annotations.Version;

import com.google.appengine.api.datastore.Key;

@Entity(Kinds.BASIC_VERSIONED_CLASS)
public class BasicVersionedClass {
	
	@Id @Property(Attrs.KEY)
	private Key key;
	
	@Version @Property(Attrs.VERSION)
	private Long version;

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
	
}