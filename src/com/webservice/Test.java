package com.webservice;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.entity.Employees;
import com.manager.Resource;

@Path("/test")
public class Test {
    
    @Inject EntityManager em;
    
    @GET
    @Path("/status")
    @Produces("application/json")
    public Response getStatus() {
        return Response.ok("{\"status\":\"Running..\"}").build();
    }
    
    @GET
    @Produces("application/json")
    public Response getEmployeess(@HeaderParam("token") String token) {
        String response = null;
        em = Resource.getEntityManager();
        Query query = em.createQuery("FROM com.entity.Employees", Employees.class);
        List<Employees> list = Resource.castList(Employees.class, query.getResultList());
        em.close();
        response = list.toString();
        return Response.ok(response).build();
    }
}
