package org.simpleds.testdb;

import java.util.Date;

import org.simpleds.annotations.Embedded;
import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;
import org.simpleds.annotations.Property;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@Entity(Kinds.DUMMY1)
public class Dummy1 {

	public enum EnumValues {
		FOO, BAR
	}
	
	@Id @Property(Attrs.KEY)
	private Key key;
	
	@Property(required=true, value=Attrs.NAME)
	private String name;
	
	@Property(required=true, value=Attrs.DATE)
	private Date overridenNameDate;
	
	@SuppressWarnings("unused") @Property(Attrs.E_VALUE)
	private EnumValues evalue;
	
	@Embedded
	private Embedded1 embedded;
	
	@Property(unindexed=true, value=Attrs.BIG_STRING)
	private String bigString;
	
	/**
	 * Create a simple dummy key
	 */
	public static Key createDummyKey() {
		return KeyFactory.createKey(Kinds.DUMMY1, 1);
	}
	
	public static Dummy1 create() {
		Dummy1 dummy = new Dummy1();
		dummy.setName("foo");
		dummy.setOverridenNameDate(new Date());
		dummy.setBigString("foobar");
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

	public String getBigString() {
		return bigString;
	}

	public void setBigString(String bigString) {
		this.bigString = bigString;
	}
	
	
	
}
