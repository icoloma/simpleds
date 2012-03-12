package org.simpleds.testdb;

import java.util.Date;

import org.simpleds.annotations.Cacheable;
import org.simpleds.annotations.Embedded;
import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;
import org.simpleds.annotations.Property;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@Cacheable(120)
@Entity
public class CacheableEntity {

	public enum EnumValues {
		FOO, BAR
	}
	
	@Id(generated=true)
	private Key key;
	
	@Property(required=true)
	private String name;
	
	@Property("date")
	private Date overridenNameDate;
	
	@Embedded
	private Embedded1 embedded;
	
	public static Key createKey() {
		return KeyFactory.createKey(CacheableEntity.class.getSimpleName(), 1);
	}
	
	public static CacheableEntity create() {
		CacheableEntity dummy = new CacheableEntity();
		dummy.setName("foo");
		dummy.setOverridenNameDate(new Date());
		return dummy;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getOverridenNameDate() {
		return overridenNameDate;
	}

	public void setOverridenNameDate(Date date) {
		this.overridenNameDate = date;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Embedded1 getEmbedded() {
		return embedded;
	}
	
}
