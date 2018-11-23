package com.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    
    /**
     * Generic cast class to avoid "unchecked" warnings.
     * @param class1
     * @param c
     * @return Object
     */
    public static <T> List<T> castList(Class<? extends T> class1, Collection<?> c) {
        List<T> r = new ArrayList<T>(c.size());
        for(Object o: c)
          r.add(class1.cast(o));
        return r;
    }
}
