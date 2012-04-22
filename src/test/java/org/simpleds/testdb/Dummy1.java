package org.simpleds.testdb;

import java.util.Date;

import org.simpleds.annotations.Embedded;
import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;
import org.simpleds.annotations.MultivaluedIndex;
import org.simpleds.annotations.Property;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@Entity("d1")
@MultivaluedIndex(name="friends", itemClass=Key.class)
public class Dummy1 {

	public static final String KIND = "d1";
	
	public enum EnumValues {
		FOO, BAR
	}
	
	@Id
	private Key key;
	
	@Property(required=true)
	private String name;
	
	@Property(required=true, value="date")
	private Date overridenNameDate;
	
	@SuppressWarnings("unused")
	private EnumValues evalue;
	
	@Embedded
	private Embedded1 embedded;
	
	@Property(unindexed=true)
	private String bigString;
	
	/**
	 * Create a simple dummy key
	 */
	public static Key createDummyKey() {
		return KeyFactory.createKey(Dummy1.KIND, 1);
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
