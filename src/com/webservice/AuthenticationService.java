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
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.entity.Employees;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.manager.Resource;

@Path("/auth")
public class AuthenticationService {

    @Inject EntityManager em;
    
    private static Map<String, Token> tokens = new HashMap<>();
    private static Set<Integer> admins = new HashSet<>();
    
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public Response authUser(String payload) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Credential cred = gson.fromJson(payload, Credential.class);
        if(verifyLoginCombo(cred.username, cred.password)) {
            em = Resource.getEntityManager();
            Query query = em.createQuery("select e from Employees e "
                    + "where e.userName = :user", Employees.class);
            query.setParameter("user", cred.username);
            List<Employees> list = Resource.castList(Employees.class, query.getResultList());
            Employees emp = list.get(0);
            if(emp == null) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
            String token = issueToken(emp.getEmpNumber());
            return Response.ok("{ \"token\": \"" + token + "\"}").build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }
    
    
    private Map<String, String> getLoginCombos() {
        em = Resource.getEntityManager();
        Query query = em.createQuery("FROM com.entity.Employees", Employees.class);
        List<Employees> list = Resource.castList(Employees.class, query.getResultList());
        em.close();
        if(admins.isEmpty()) {
            for(Employees e : list) {
                if(e.getIsAdmin()) {
                    admins.add(e.getEmpNumber());
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        for(Employees e : list) {
            map.put(e.getUserName(), e.getPassword());
        }
        return map;
    }
    
    private boolean verifyLoginCombo(String username, String password) {
        Map<String, String> combos = getLoginCombos();
        if (combos.containsKey(username)) {
            if (combos.get(username).equals(password)) {
                return true;
            }
        }
        return false;
    }
    
    public static Auth verifyToken(String tokenId) {
        if(tokens.containsKey(tokenId)) {
            if(tokens.get(tokenId).expires <= System.currentTimeMillis()) {
                tokens.remove(tokenId);
                return null;
            } else {
                Token t = tokens.get(tokenId);
                return new Auth(t.empNumber, t.isAdmin);
            }
        }
        return null;
    }

    private String issueToken(int empNumber) {
        Random rand = new SecureRandom();
        String tokenId = new BigInteger(64, rand).toString();
        Token token = new Token(empNumber);
        if(admins.contains(empNumber)) {
            token.isAdmin = true;
        }
        tokens.put(tokenId, token);
        return tokenId;
    }
    
    class Token {
        static final long ONE_HOUR = 3600000;
        int empNumber;
        boolean isAdmin = false;
        long expires;
        Token(int empNumber) {
            this.empNumber = empNumber;
            expires = System.currentTimeMillis() + ONE_HOUR;
        }
    }
    
    class Credential {
        String username;
        String password;
    }
    
}
