package org.simpleds;

import java.util.Date;

import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;
import org.simpleds.annotations.Property;
import org.simpleds.annotations.Version;

import com.google.appengine.api.datastore.Key;
import org.simpleds.testdb.Attrs;
import org.simpleds.testdb.Kinds;

public class DateVersionManagerTest extends AbstractVersionManagerTest {

	@Override
	protected Object createEntity() {
		return new DateVersionedClass();
	}

    @Entity(Kinds.DATE_VERSIONED_CLASS)
	public static class DateVersionedClass {

		@Id @Property(Attrs.KEY)
		private Key key;
		
		@Version @Property(Attrs.VERSION)
		private Date version;

		public Key getKey() {
			return key;
		}

		public void setKey(Key key) {
			this.key = key;
		}

		public Date getVersion() {
			return version;
		}

		public void setVersion(Date version) {
			this.version = version;
		}

		
	}

	
}
