package org.simpleds.schema.action;

import java.util.Map;

import org.simpleds.schema.AbstractDatastoreAction;

/**
 * Doe snot do anything
 * @author icoloma
 *
 */
public class MockAction extends AbstractDatastoreAction {

	public MockAction() {
		super("mock-action");
	}

	@Override
	public long proceed(String uri, Map<String, String> params) {
		return 0;
	}
	
}
