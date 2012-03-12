package org.simpleds;

import java.util.Date;

import org.simpleds.annotations.Id;
import org.simpleds.annotations.Version;

import com.google.appengine.api.datastore.Key;

public class DateVersionManagerTest extends AbstractVersionManagerTest {

	@Override
	protected Object createEntity() {
		return new DateVersionedClass();
	}
	
	public static class DateVersionedClass {
		@Id
		private Key key;
		
		@Version
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
