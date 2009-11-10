package org.simpleds.testdb;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.simpleds.annotations.MultivaluedIndex;

import com.google.appengine.api.datastore.Key;

@Entity
@MultivaluedIndex(name="friends", itemClass=Key.class)
public class Dummy1 {

	enum EnumValues {
		FOO, BAR
	}
	
	@Id
	@GeneratedValue
	private Key key;
	
	@Basic(optional=false)
	private String name;
	
	@Column(nullable=false)
	private Date date;

	@SuppressWarnings("unused")
	private EnumValues evalue;
	
	@Embedded
	private Embedded1 embedded;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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
