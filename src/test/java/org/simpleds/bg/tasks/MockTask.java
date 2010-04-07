package org.simpleds.bg.tasks;

import java.util.Map;

import org.simpleds.bg.AbstractBackgroundTask;

/**
 * Doe snot do anything
 * @author icoloma
 *
 */
public class MockTask extends AbstractBackgroundTask {

	public MockTask() {
		super("mock-action");
	}

	@Override
	public long doProceed(String uri, Map<String, String> params) {
		return 0;
	}
	
}
