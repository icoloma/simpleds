package org.simpleds;

import org.simpleds.annotations.Id;
import org.simpleds.annotations.Version;

import com.google.appengine.api.datastore.Key;

public class PrimitiveLongVersionManagerTest extends AbstractVersionManagerTest {

	public PrimitiveLongVersionManagerTest() {
		defaultValue = 0L;
	}
	
	@Override
	protected Object createEntity() {
		return new VersionedClass();
	}
	
	public static class VersionedClass {
		@Id
		private Key key;
		
		@Version
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
