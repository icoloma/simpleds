package org.simpleds;


/**
 * Creates a {@link EntityManager} instance. 
 * @author icoloma
 */
public final class EntityManagerFactory {

	private static EntityManager instance;
	
	private EntityManagerFactory() {}
	
	public static EntityManager getEntityManager() {
		return instance;
	}

	static void setEntityManager(EntityManager instance) {
		EntityManagerFactory.instance = instance;
	}

}
