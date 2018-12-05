package com.webservice;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.entity.Employees;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.manager.Resource;

/**
 * Defining REST URI calls for employee-related functionality.
 * @author Danny
 * @version 1.0
 *
 */
@Path("/employees")
public class EmployeeService {

    /** Persistence entity manager object. */
    @Inject private EntityManager em;

    /**
     * Gets all employees in the database.
     * @param token user token
     * @return all employee data
     */
    @GET
    @Produces("application/json")
    public Response getEmployees(@HeaderParam("token") String token) {
        String response = null;
        Auth auth = AuthenticationService.verifyToken(token);
        if (auth == null) {
            //failed to authenticate 
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else if (!auth.isAdmin()) {
            //user is not admin
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        em = Resource.getEntityManager();
        Query query = em.createQuery("FROM com.entity.Employees", 
                Employees.class);
        List<Employees> list = Resource.castList(Employees.class, 
                query.getResultList());
        em.close();
        response = list.toString();
        return Response.status(Response.Status.OK).entity(response).build();
    }

    /**
     * Gets a single employee by E number.
     * @param empNumber employee number
     * @param token user token
     * @return single employee record
     */
    @GET
    @Path("{empNumber}")
    @Produces("application/json")
    public Response getEmployee(@PathParam("empNumber") int empNumber, 
            @HeaderParam("token") String token) {
        Auth auth = AuthenticationService.verifyToken(token);
        if (auth == null) {
            //failed to authenticate 
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else if (!auth.isAdmin()) {
            //user is not admin
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        
        em = Resource.getEntityManager();
        Employees employee = em.find(Employees.class, empNumber);
        if (employee == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK)
                .entity(employee.toString()).build();
    }

    /**
     * Updates an employee record.
     * @param empNumber employee number
     * @param payload employee data
     * @param token user token
     * @return Copy of the edited employee
     */
    @Transactional
    @PUT
    @Path("{empNumber}")
    @Consumes("application/json")
    public Response updateEmployee(@PathParam("empNumber") int empNumber, 
            String payload, @HeaderParam("token") String token) {
        Auth auth = AuthenticationService.verifyToken(token);
        if (auth == null) {
            //failed to authenticate 
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else if (!auth.isAdmin()) {
            //user is not admin
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Employees employee = gson.fromJson(payload, Employees.class);

        System.out.println(employee);
        em = Resource.getEntityManager();
        em.getTransaction().begin();
        Employees entity = em.find(Employees.class, empNumber);
        String returnCode = "";

        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else if (entity.equals(employee)) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        try {
            entity.setFirstName(employee.getFirstName());
            entity.setLastName(employee.getLastName());
            entity.setUserName(employee.getUserName());
            entity.setPassword(employee.getPassword());
            entity.setIsAdmin(employee.getIsAdmin());
            em.persist(entity);
            em.flush();
            em.getTransaction().commit();
            em.close();
            returnCode = employee.toString();
        } catch (WebApplicationException err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"500\"," + "\"message\":\"Resource"
                    + " not created.\"" + "\"developerMessage\":\""
                    + err.getMessage() + "\"" + "}";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(returnCode).build();

        }
        return Response.status(Response.Status.OK).entity(returnCode).build();
    }

    /**
     * Creates a new employee record.
     * @param payload employee data
     * @param token user token
     * @return copy of new employee
     */
    @Transactional
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createEmployee(String payload, 
            @HeaderParam("token") String token) {
        System.out.println("payload - " + payload);
        Auth auth = AuthenticationService.verifyToken(token);
        if (auth == null) {
            //failed to authenticate 
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else if (!auth.isAdmin()) {
            //user is not admin
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        Employees employee = gson.fromJson(payload, Employees.class);
        System.out.println(employee);
        String returnCode = "200";
        em = Resource.getEntityManager();

        try {
            em.getTransaction().begin();
            em.persist(employee);
            em.flush();
            em.refresh(employee);
            em.getTransaction().commit();
            em.close();

            returnCode = employee.toString();
        } catch (WebApplicationException err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"400\"," + "\"message\":\"Resource"
                    + " not created.\"" + "\"developerMessage\":\""
                    + err.getMessage() + "\"" + "}";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(returnCode).build();

        }
        return Response.status(Response.Status.CREATED)
                .entity(returnCode).build();
    }

    /**
     * Deletes an employee.
     * @param empNumber employee number
     * @param token user token
     * @return Response code
     */
    @Transactional
    @DELETE
    @Consumes("application/json")
    @Produces("application/json")
    @Path("{empNumber}")
    public Response deleteEmployee(@PathParam("empNumber") int empNumber, 
            @HeaderParam("token") String token) {
        Auth auth = AuthenticationService.verifyToken(token);
        if (auth == null) {
            //failed to authenticate 
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else if (!auth.isAdmin()) {
            //user is not admin
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        em = Resource.getEntityManager();
        String returnCode = "";

        try {
            em.getTransaction().begin();
            Employees existingEmployee = em.find(Employees.class, empNumber);
            em.remove(existingEmployee);
            em.getTransaction().commit();
            em.close();
            returnCode = "{" + "\"message\":\"Employee number " 
                    + existingEmployee.getEmpNumber() + "succesfully deleted\""
                    + "}";
        } catch (WebApplicationException err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"404\"," + "\"message\":\"Resource"
                    + " not deleted.\"" + "\"developerMessage\":\""
                    + err.getMessage() + "\"" + "}";
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(returnCode).build();
        }
        return Response.status(Response.Status.NO_CONTENT)
                .entity(returnCode).build();
    }
}
