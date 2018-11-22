package com.manager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.Produces;

/**
 * Allows access to persistence entity manager.
 * @author Danny
 * @version 1.0
 */
public class Resource {
    /** Persistence entity manager. */
    private static EntityManagerFactory mgr;
    
    /**
     * Getter for entity manager.
     * @return entity manager
     */
    @Produces
    public static EntityManager getEntityManager() {
        if (mgr == null) {
            mgr = Persistence.createEntityManagerFactory("db3");
        }
        EntityManager em = mgr.createEntityManager();
        return em;
    }
}
