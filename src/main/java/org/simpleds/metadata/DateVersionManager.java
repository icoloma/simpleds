package org.simpleds.metadata;

import java.util.Date;

/**
 * Uses the current timestamp as version value. 
 * @author icoloma
 *
 */
public class DateVersionManager extends AbstractVersionManager<Date> {

	@Override
	public Date nextValue(Date currentValue) {
		return new Date();
	}
	
	@Override
	public Object getStartValue() {
		return new Date();
	}

}
