package org.simpleds;

import org.simpleds.annotations.Entity;
import org.simpleds.annotations.Id;
import org.simpleds.annotations.Property;
import org.simpleds.annotations.Version;

import com.google.appengine.api.datastore.Key;
import org.simpleds.testdb.Attrs;
import org.simpleds.testdb.Kinds;

public class PrimitiveLongVersionManagerTest extends AbstractVersionManagerTest {

	public PrimitiveLongVersionManagerTest() {
		defaultValue = 0L;
	}
	
	@Override
	protected Object createEntity() {
		return new VersionedClass();
	}

    @Entity(Kinds.PRIMITIVE_VERSIONED_CLASS)
	public static class VersionedClass {

		@Id @Property(Attrs.KEY)
		private Key key;
		
		@Version @Property(Attrs.VERSION)
		private long version;

		public Key getKey() {
			return key;
		}

		public void setKey(Key key) {
			this.key = key;
		}

		public long getVersion() {
			return version;
		}

		public void setVersion(long version) {
			this.version = version;
		}
		
	}
	
}
