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
    @Inject EntityManager em;

    @GET
    @Path("/status")
    @Produces("application/json")
    public Response getStatus() {
        return Response.ok("{\"status\":\"Running..\"}").build();
    }

    @GET
    @Produces("application/json")
    public Response getEmployeess() {
        String response = null;
        em = Resource.getEntityManager();
        Query query = em.createQuery("FROM com.entity.Employees", Employees.class);
        List<Employees> list = Resource.castList(Employees.class, query.getResultList());
        em.close();
        response = list.toString();
        return Response.ok(response).build();
    }

    @GET
    @Path("{empNumber}")
    @Produces("application/json")
    public Response getEmployees(@PathParam("empNumber") int empNumber) {
        em = Resource.getEntityManager();
        Employees employee = em.find(Employees.class, empNumber);
        if (employee == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return Response.ok(employee.toString()).build();
    }

    @Transactional
    @PUT
    @Path("{empNumber}")
    @Consumes("application/json")
    public Response updateEmployees(@PathParam("empNumber") int empNumber, String payload) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Employees employee = gson.fromJson(payload, Employees.class);

        System.out.println(employee);
        em = Resource.getEntityManager();
        em.getTransaction().begin();
        Employees entity = em.find(Employees.class, empNumber);
        String returnCode = "";

        if (entity == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        try {
            entity.setName(employee.getName());
            entity.setUserName(employee.getUserName());
            entity.setPassword(employee.getPassword());
            entity.setIsAdmin(employee.getIsAdmin());
            em.persist(entity);
            em.flush();
            em.getTransaction().commit();
            em.close();
            returnCode = "{" + "\"href\":\"http://localhost:8080/rest/employeeservice/employees/" + employee.getName()
                    + "\"," + "\"message\":\"Employees successfully edited.\"" + "}";
        } catch (Exception err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"500\"," + "\"message\":\"Resource not created.\"" + "\"developerMessage\":\""
                    + err.getMessage() + "\"" + "}";
            return Response.status(404).entity(returnCode).build();

        }
        return Response.status(201).entity(returnCode).build();
    }

    @Transactional
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createEmployees(String payload) {
        System.out.println("payload - " + payload);

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

            returnCode = "{" + "\"href\":\"http://localhost:8080/rest/employeeservice/employee/" + employee.getName()
                    + "\"," + "\"message\":\"New Employees successfully created.\"" + "}";
        } catch (Exception err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"500\"," + "\"message\":\"Resource not created.\"" + "\"developerMessage\":\""
                    + err.getMessage() + "\"" + "}";
            return Response.status(404).entity(returnCode).build();

        }
        return Response.status(201).entity(returnCode).build();
    }

    @Transactional
    @DELETE
    @Consumes("application/json")
    @Produces("application/json")
    @Path("{empNumber}")
    public Response deleteEmployees(@PathParam("empNumber") int empNumber) {
        em = Resource.getEntityManager();
        String returnCode = "";

        try {
            em.getTransaction().begin();
            Employees existingEmployees = em.find(Employees.class, empNumber);
            em.remove(existingEmployees);
            em.getTransaction().commit();
            em.close();
            returnCode = "{" + "\"message\":\"Employees succesfully deleted\"" + "}";
        } catch (Exception err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"500\"," + "\"message\":\"Resource not deleted.\"" + "\"developerMessage\":\""
                    + err.getMessage() + "\"" + "}";
            return Response.status(500).entity(returnCode).build();
        }
        return Response.ok(returnCode).build();
    }
}
