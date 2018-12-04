package com.webservice;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.entity.Employees;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.manager.Resource;

/**
 * Defining REST URI calls for login authorization functionality.
 * @author Tony
 * @version 1.0
 *
 */
@Path("/auth")
public class AuthenticationService {
    
    /** Map of active authorization tokens. */
    private static Map<String, Token> tokens = new HashMap<>();
    
    /** Set containing admins. */
    private static Set<Integer> admins = new HashSet<>();
    
    /** Number used for generating random token. */
    private static final int TOKENNUM = 64;
    
    /** Persistence entity manager object. */
    @Inject private EntityManager em;

    /**
     * Logs in a user if username and password are correct, and
     * supplies them with a token.
     * @param payload login credentials
     * @return Response with token
     */
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public Response authUser(String payload) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Credential cred = gson.fromJson(payload, Credential.class);
        if (verifyLoginCombo(cred.username, cred.password)) {
            em = Resource.getEntityManager();
            Query query = em.createQuery("select e from Employees e "
                    + "where e.userName = :user", Employees.class);
            query.setParameter("user", cred.username);
            List<Employees> list = Resource.castList(Employees.class,
                    query.getResultList());
            Employees emp = list.get(0);
            if (emp == null) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
            String token = issueToken(emp.getEmpNumber());
            return Response.ok("{ \"token\": \"" + token + "\"}").build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }
    
    /**
     * Retrieves username and password combos from database.
     * @return map of combos
     */
    private Map<String, String> getLoginCombos() {
        em = Resource.getEntityManager();
        Query query = em.createQuery("FROM com.entity.Employees", 
                Employees.class);
        List<Employees> list = Resource.castList(Employees.class, 
                query.getResultList());
        em.close();
        if (admins.isEmpty()) {
            for (Employees e : list) {
                if (e.getIsAdmin()) {
                    admins.add(e.getEmpNumber());
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        for (Employees e : list) {
            map.put(e.getUserName(), e.getPassword());
        }
        return map;
    }
    
    /**
     * Verifies a username and password.
     * @param username username input
     * @param password password input
     * @return true if valid
     */
    private boolean verifyLoginCombo(String username, String password) {
        Map<String, String> combos = getLoginCombos();
        if (combos.containsKey(username)) {
            if (combos.get(username).equals(password)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifies if token is still valid.
     * @param tokenId token input
     * @return true if token is still valid
     */
    public static Auth verifyToken(String tokenId) {
        if (tokens.containsKey(tokenId)) {
            if (tokens.get(tokenId).expires <= System.currentTimeMillis()) {
                tokens.remove(tokenId);
                return null;
            } else {
                Token t = tokens.get(tokenId);
                return new Auth(t.empNumber, t.isAdmin);
            }
        }
        return null;
    }

    /**
     * Inserts a new token into token map when a user logs in.
     * @param empNumber logged in user
     * @return new token
     */
    private String issueToken(int empNumber) {
        Random rand = new SecureRandom();
        String tokenId = new BigInteger(TOKENNUM, rand).toString();
        Token token = new Token(empNumber);
        if (admins.contains(empNumber)) {
            token.isAdmin = true;
        }
        tokens.put(tokenId, token);
        return tokenId;
    }
    
    /**
     * A class representing a single token.
     * @author Tony
     * @version 1
     *
     */
    class Token {
        /** One hour. */
        static final long ONE_HOUR = 3600000;
        /** Employee number. */
        private int empNumber;
        /** Admin status of employee. */
        private boolean isAdmin;
        /** Time when token expires. */
        private long expires;
        
        /**
         * Constructor.
         * @param empNumber employee requesting token
         */
        Token(int empNumber) {
            this.empNumber = empNumber;
            expires = System.currentTimeMillis() + ONE_HOUR;
        }
    }
    
    /**
     * A class representing login credentials.
     * @author Tony
     * @version 1
     *
     */
    class Credential {
        /** Employee's username. */
        private String username;
        /** Employee's password. */
        private String password;
    }
    
}
