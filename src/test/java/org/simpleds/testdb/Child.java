package org.simpleds.testdb;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.simpleds.annotations.Entity;

import com.google.appengine.api.datastore.Key;

@Entity(parent=Root.class)
public class Child {

	@Id @GeneratedValue
	private Key key;

	public Key getKey() {
		return key;
	}

	public void setKey(Key id) {
		this.key = id;
	}
	
}
