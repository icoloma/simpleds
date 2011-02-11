package org.simpleds;

import org.simpleds.testdb.VersionedClass;




public class LongVersionManagerTest extends AbstractVersionManagerTest {

	@Override
	protected Object createEntity() {
		return new VersionedClass();
	}
	
}
