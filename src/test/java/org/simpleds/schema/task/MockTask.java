package org.simpleds.schema.task;

import java.util.Map;

import org.simpleds.schema.AbstractTask;

/**
 * Doe snot do anything
 * @author icoloma
 *
 */
public class MockTask extends AbstractTask {

	public MockTask() {
		super("mock-action");
	}

	@Override
	public long doProceed(String uri, Map<String, String> params) {
		return 0;
	}
	
}
