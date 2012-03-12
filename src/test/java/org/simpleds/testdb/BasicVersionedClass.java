package org.simpleds.testdb;

import org.simpleds.annotations.Id;
import org.simpleds.annotations.Version;

import com.google.appengine.api.datastore.Key;

public class BasicVersionedClass {
	
	@Id
	private Key key;
	
	@Version
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