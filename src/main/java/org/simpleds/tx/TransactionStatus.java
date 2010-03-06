package org.simpleds.tx;

import com.google.appengine.api.datastore.Transaction;

/**
 * The status of transactions  associated to the current thread
 * @author icoloma
 *
 */
class TransactionStatus {
	
	/** the nesting level of transactional calls */
	private int currentNestingLevel;

	/**
	 * Adds one level of interception for the current thread
	 * @return the current level of interception before invoking push(), 0 if this is the first call
	 */
	public int pushContext() {
		return currentNestingLevel++;
	}
	
	/**
	 * Substracts one level of interception for the current thread
	 * @return the current level of interception after invoking pop(), 0 if this is the last expected invocation to pop()
	 */
	public int popContext() {
		return --currentNestingLevel;
	}

	public void add(Transaction transaction) {
		if (currentNestingLevel <= 0) {
			throw new IllegalStateException("TransactionManager.pushContext() has not yet been invoked. Is TransactionInterceptor properly configured?");
		}
	}
	
}
