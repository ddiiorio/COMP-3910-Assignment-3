package com.webservice;

import javax.ws.rs.Consumes;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public Response authUser(String payload) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Credential cred = gson.fromJson(payload, Credential.class);
        
        if(verifyLoginCombo(cred.empNumber, cred.password)) {
            String token = issueToken(cred.empNumber);
            return Response.ok("{ \"token\": \"" + token + "\"}").build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }
    
    
    public Map<Integer, String> getLoginCombos() {
        em = Resource.getEntityManager();
        Query query = em.createQuery("FROM com.entity.Employees", Employees.class);
        List<Employees> list = Resource.castList(Employees.class, query.getResultList());
        em.close();
        Map<Integer, String> map = new HashMap<>();
        for(Employees e : list) {
            map.put(e.getEmpNumber(), e.getPassword());
        }
        return map;
    }
    
    private boolean verifyLoginCombo(int empNumber, String password) {
        Map<Integer, String> combos = getLoginCombos();
        if (combos.containsKey(empNumber)) {
            if (combos.get(empNumber).equals(password)) {
                return true;
            }
        }
        return false;
    }
    
    public static int verifyToken(String tokenId) {
        if(tokens.containsKey(tokenId)) {
            if(tokens.get(tokenId).expires <= System.currentTimeMillis()) {
                tokens.remove(tokenId);
                return -1;
            } else {
                return tokens.get(tokenId).empNumber;
            }
        }
        return -1;
    }

    private String issueToken(int empNumber) {
        Random rand = new SecureRandom();
        String tokenId = new BigInteger(64, rand).toString();
        Token token = new Token(empNumber);
        tokens.put(tokenId, token);
        return tokenId;
    }
    
    class Token {
        static final long ONE_HOUR = 3600000;
        Token(int empNumber){
            this.empNumber = empNumber;
            expires = System.currentTimeMillis() + ONE_HOUR;
        }
        int empNumber;
        long expires;
    }
    
}
