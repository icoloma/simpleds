package org.simpleds.testdb;

import com.google.appengine.api.datastore.Key;
import org.simpleds.annotations.AsJSON;
import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;
import org.simpleds.annotations.Property;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity(Kinds.JSON_STORED)
public class JsonStored {

    @Id @Property(Attrs.KEY)
    private Key key;

	@AsJSON @Property(Attrs.LIST)
	private List<Dummy1> list;

	@AsJSON @Property(Attrs.SET)
	private Set<String> set;

	@AsJSON @Property(Attrs.MAP)
	private Map<String, Dummy1> map;

    @AsJSON @Property(Attrs.POLY)
    private List<Plugin> polyList;

    public List<Plugin> getPolyList() {
        return polyList;
    }

    public void setPolyList(List<Plugin> polyList) {
        this.polyList = polyList;
    }

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
