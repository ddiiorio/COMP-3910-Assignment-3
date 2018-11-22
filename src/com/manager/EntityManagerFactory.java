package com.manager;

import java.io.Serializable;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Manages the persistence entity manager.
 * @author Danny
 * @version 1.0
 *
 */
public class EntityManagerFactory implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** Reference to Resource class. */
    @Inject private Resource resource;
    
    /**
     * Getter for entity manager.
     * @return entity manager
     */
    @SuppressWarnings("static-access")
    public EntityManager gEntityManager() {
        return resource.getEntityManager();
    }
}
