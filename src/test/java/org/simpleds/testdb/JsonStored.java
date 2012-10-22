package org.simpleds.testdb;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.simpleds.annotations.AsJSON;
import org.simpleds.annotations.Id;

import com.google.appengine.api.datastore.Key;

public class JsonStored {

	@Id
	private Key key;
	
	@AsJSON
	private List<Dummy1> list;
	
	@AsJSON
	private Set<String> set;
	
	@AsJSON
	private Map<String, Dummy1> map;

	

	public List<Dummy1> getList() {
		return list;
	}

	public void setList(List<Dummy1> list) {
		this.list = list;
	}

	public Set<String> getSet() {
		return set;
	}

	public void setSet(Set<String> set) {
		this.set = set;
	}

	public Map<String, Dummy1> getMap() {
		return map;
	}

	public void setMap(Map<String, Dummy1> map) {
		this.map = map;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

}
