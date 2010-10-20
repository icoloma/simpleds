package org.simpleds;


/**
 * Creates a {@link IndexManager} instance
 * @author icoloma
 */
public final class IndexManagerFactory {

	private static IndexManager instance;
	
	private IndexManagerFactory() {}
	
	public static IndexManager getIndexManager() {
		return instance;
	}

	public static void setIndexManager(IndexManager indexManager) {
		instance = indexManager;
	}

}
