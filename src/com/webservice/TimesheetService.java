package com.webservice;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.entity.Timesheet;
import com.entity.TimesheetRow;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.manager.Resource;

import javafx.util.Pair;


/**
 * Defining REST URI calls for timesheet-related functionality.
 * @author Danny
 * @version 1.0
 *
 */
@Path("/timesheets")
public class TimesheetService {

    /** Persistence entity manager object. */
    @Inject private EntityManager em;
    
    /** Map to hold all timesheets in GET request. */
    private Map<Timesheet, List<TimesheetRow>> completeTimesheets 
        = new HashMap<>();

    /**
     * Gets all timesheets in the database.
     * @param token user token
     * @return all timesheet data
     */
    @GET
    @Produces("application/json")
    public Response getTimesheets(@HeaderParam("token") String token) {
        Auth auth = AuthenticationService.verifyToken(token);
        if (auth == null) {
            //failed to authenticate 
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else if (!auth.isAdmin()) {
            //user is not admin
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String response = null;
        em = Resource.getEntityManager();
        
        Query tsQuery = em.createQuery("FROM com.entity.Timesheet", 
                Timesheet.class);
        List<Timesheet> tsList = Resource.castList(Timesheet.class, 
                tsQuery.getResultList());
        
        for (Timesheet timesheet : tsList) {
            Query tsRowQuery = em.createQuery("select t from TimesheetRow t "
                    + "where t.timesheetId = :id", TimesheetRow.class)
                    .setParameter("id", timesheet.getTimesheetId());
            List<TimesheetRow> tsRowList = Resource.castList(TimesheetRow.class,
                    tsRowQuery.getResultList());
            completeTimesheets.put(timesheet, tsRowList);
        }
        em.close();
        response = completeTimesheets.toString();
        return Response.status(Response.Status.OK).entity(response).build();
    }

    /**
     * Gets a single timesheet by ID.
     * @param timesheetId Timesheet ID
     * @param token user token
     * @return single timesheet record
     */
    @GET
    @Path("{timesheetId}")
    @Produces("application/json")
    public Response getTimesheet(@PathParam("timesheetId") int timesheetId,
            @HeaderParam("token") String token) {
        Auth auth = AuthenticationService.verifyToken(token);
        if (auth == null) {
            //failed to authenticate or user is not admin
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String response = null;
        em = Resource.getEntityManager();
        Timesheet timesheet = em.find(Timesheet.class, timesheetId); 
        Query query = em.createQuery("select t from TimesheetRow t "
                + "where t.timesheetId = :id", TimesheetRow.class)
                .setParameter("id", timesheetId);
        List<TimesheetRow> list = Resource.castList(TimesheetRow.class,
                query.getResultList());
        if (timesheet == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        em.close();
        if (!auth.isAdmin() || auth.getEmpNumber() 
                != timesheet.getEmpNumber()) {
            //user is not admin, or does not own this timesheet
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        List<Object> test = new ArrayList<>();
        test.add(timesheet);
        test.add(list);
        response = test.toString();
        return Response.status(Response.Status.OK).entity(response).build();
    }

    /**
     * Updates an existing timesheet.
     * @param timesheetId Timesheet ID
     * @param token user token
     * @param payload timesheet data
     * @return Copy of edited timesheet
     */
    @Transactional
    @PUT
    @Path("{timesheetId}")
    @Consumes("application/json")
    public Response updateTimesheet(@PathParam("timesheetId") int timesheetId, 
            @HeaderParam("token") String token, String payload) {
        Auth auth = AuthenticationService.verifyToken(token);
        if (auth == null) {
            //failed to authenticate or user is not admin
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Timesheet timesheet = gson.fromJson(payload, Timesheet.class);

        if (timesheet.getEmpNumber() != auth.getEmpNumber()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        
        System.out.println(timesheet);
        em = Resource.getEntityManager();
        em.getTransaction().begin();
        Timesheet entity = em.find(Timesheet.class, timesheetId);
        String returnCode = "";

        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            entity.setEmpNumber(timesheet.getEmpNumber());
            entity.setEndWeek(timesheet.getEndWeek());
            entity.setOvertime(timesheet.getOvertime());
            entity.setFlextime(timesheet.getFlextime());
            em.persist(entity);
            em.flush();
            em.getTransaction().commit();
            em.close();
            returnCode = "{" + "\"href\":\"http://localhost:8080/rest/"
                    + "timesheets/" + timesheet.getTimesheetId()
                    + "\"," + "\"message\":\"Timesheet successfully"
                            + " edited.\"" + "}";
        } catch (WebApplicationException err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"500\"," + "\"message\":\"Resource "
                    + "not created.\"" + "\"developerMessage\":\""
                    + err.getMessage() + "\"" + "}";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(returnCode).build();

        }
        return Response.status(Response.Status.OK).entity(returnCode).build();
    }

    /**
     * Creates a new timesheet.
     * @param token user token
     * @param payload timesheet data
     * @return copy of new timesheet
     */
    @Transactional
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createTimesheet(@HeaderParam("token") String token,
            String payload) {
        System.out.println("payload - " + payload);
        
        Auth auth = AuthenticationService.verifyToken(token);
        if (auth == null) {
            //failed to authenticate or user is not admin
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        Timesheet timesheet = gson.fromJson(payload, Timesheet.class);
        System.out.println(timesheet);
        
        if (timesheet.getEmpNumber() != auth.getEmpNumber()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        
        String returnCode = "";
        em = Resource.getEntityManager();

        try {
            em.getTransaction().begin();
            em.persist(timesheet);
            em.flush();
            em.refresh(timesheet);
            em.getTransaction().commit();
            em.close();

            returnCode = "{" + timesheet.getTimesheetId()
                    + "\"," + ":\"New Timesheet successfully created.\"" + "}";
        } catch (WebApplicationException err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"500\"," + "\"message\":\"Resource"
                    + " not created.\"" + "\"developerMessage\":\""
                    + err.getMessage() + "\"" + "}";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(returnCode).build();

        }
        return Response.status(Response.Status.CREATED)
                .entity(returnCode).build();
    }
    
    /**
     * Creates a new timesheet row.
     * @param token user token
     * @param payload timesheet row data
     * @return copy of new timesheet row
     */
    @Transactional
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createRow(@HeaderParam("token") String token, 
            String payload) {
        //TODO:Write this method lol
        return null;
    }

    /**
     * Deletes a timesheet.
     * @param timesheetId Timesheet ID
     * @param token user token
     * @return Response code
     */
    @Transactional
    @DELETE
    @Consumes("application/json")
    @Produces("application/json")
    @Path("{timesheetId}")
    public Response deleteTimesheet(@PathParam("timesheetId") int timesheetId,
            @HeaderParam("token") String token) {
        Auth auth = AuthenticationService.verifyToken(token);
        if (auth == null) {
            //failed to authenticate 
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } 
        em = Resource.getEntityManager();
        String returnCode = "";
        
        try {
            em.getTransaction().begin();
            Timesheet existingTimesheet = em.find(Timesheet.class, timesheetId);
            if (existingTimesheet.getEmpNumber() != auth.getEmpNumber()) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
            em.remove(existingTimesheet);
            em.getTransaction().commit();
            em.close();
            returnCode = "{" + "\"message\":\"Timesheet succesfully"
                    + " deleted\"" + "}";
        } catch (WebApplicationException err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"500\"," + "\"message\":\"Resource"
                    + " not deleted.\"" + "\"developerMessage\":\""
                    + err.getMessage() + "\"" + "}";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(returnCode).build();
        }
        return Response.status(Response.Status.NO_CONTENT)
                .entity(returnCode).build();
    }
}
