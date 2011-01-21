package org.simpleds.metadata;


/**
 * Increments the current version value. Starts at 0.
 * @author icoloma
 *
 */
public class LongVersionManager extends AbstractVersionManager<Long> {

	@Override
	protected Long nextValue(Long currentValue) {
		return currentValue == null? getStartValue() : currentValue + 1;
	}

	@Override
	public Long getStartValue() {
		return 0L;
	}
	
}
