package org.simpleds;

import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;
import org.simpleds.annotations.Property;
import org.simpleds.annotations.Version;

import com.google.appengine.api.datastore.Key;
import org.simpleds.testdb.Attrs;
import org.simpleds.testdb.Kinds;

public class LongVersionManagerTest extends AbstractVersionManagerTest {

	@Override
	protected Object createEntity() {
		return new LongVersionedClass();
	}

    @Entity(Kinds.LONG_VERSIONED_CLASS)
	public static class LongVersionedClass {
		
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
}
