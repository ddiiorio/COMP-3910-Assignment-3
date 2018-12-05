package com.webservice;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
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
import com.entity.Timesheet;
import com.entity.TimesheetRow;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.manager.Resource;

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
        if (!auth.isAdmin() && auth.getEmpNumber() 
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
        
        String returnCode = "";
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Timesheet timesheet = gson.fromJson(payload, Timesheet.class);

        if (timesheet.getEmpNumber() != auth.getEmpNumber()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        
        if (timesheet.getTimesheetId() != timesheetId) {
            //invalid put 
            returnCode = "{\"status\":\"400\"," 
            + "\"message\":\"Unable to modify timesheet id.\"}";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(returnCode).build();
        }

        em = Resource.getEntityManager();
        em.getTransaction().begin();
        Timesheet entity = em.find(Timesheet.class, timesheetId);
        
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
            returnCode = timesheet.toString();
        } catch (WebApplicationException err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"400\"," + "\"message\":\"Resource "
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
        String returnCode = "";
        Auth auth = AuthenticationService.verifyToken(token);
        if (auth == null) {
            //failed to authenticate or user is not admin
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        Timesheet timesheet = gson.fromJson(payload, Timesheet.class);
        if (!timesheetIsValid(timesheet)) {
            //INVALID TIMESHEET DONT POST
            returnCode = "{\"status\":\"400\"," + "\"message\":\"Resource"
                    + " not created.\"}";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(returnCode).build();
        }
        
        if (!auth.isAdmin() && auth.getEmpNumber() 
                != timesheet.getEmpNumber()) {
            //user is not admin, or does not own this timesheet
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        em = Resource.getEntityManager();

        try {
            em.getTransaction().begin();
            em.persist(timesheet);
            em.flush();
            em.refresh(timesheet);
            em.getTransaction().commit();
            em.close();

            returnCode = timesheet.toString();
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
    
    private boolean timesheetIsValid(Timesheet t) {
        return t.getEndWeek() != null
                && t.getFlextime() != null
                && t.getOvertime() != null
                && t.getEmpNumber() != 0;
    }

    /**
     * Updates an existing timesheet row.
     * @param tsRowId Timesheet Row ID
     * @param token user token
     * @param payload timesheet row data
     * @return Copy of edited timesheet row
     */
    @Transactional
    @PUT
    @Path("/row/{tsRowId}")
    @Consumes("application/json")
    public Response updateTimesheetRow(@PathParam("tsRowId") int tsRowId,
            @HeaderParam("token") String token, String payload) {
        
        Auth auth = AuthenticationService.verifyToken(token);
        if (auth == null) {
            //failed to authenticate or user is not admin
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        String returnCode = "";
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        TimesheetRow tsRow = gson.fromJson(payload, TimesheetRow.class);

        if (tsRow.getTimesheetId() != tsRowId) {
            //invalid put 
            returnCode = "{\"status\":\"400\"," 
            + "\"message\":\"Unable to modify timesheet row id.\"}";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(returnCode).build();
        }
        
        em = Resource.getEntityManager();
        Timesheet timesheet = em.find(Timesheet.class, tsRow.getTimesheetId());

        if (!auth.isAdmin() && auth.getEmpNumber() 
                != timesheet.getEmpNumber()) {
            //user is not admin, or does not own this timesheet
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        em.getTransaction().begin();
        TimesheetRow entity = em.find(TimesheetRow.class, tsRowId);

        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            entity = tsBuilder(tsRow, entity);
            em.persist(entity);
            em.flush();
            em.getTransaction().commit();
            em.close();
            returnCode = tsRow.toString();
        } catch (WebApplicationException err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"400\"," + "\"message\":\"Resource "
                    + "not created.\"" + "\"developerMessage\":\""
                    + err.getMessage() + "\"" + "}";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(returnCode).build();

        }
        return Response.status(Response.Status.OK).entity(returnCode).build();
    }
    
    /**
     * Builder method to shorten timesheet row PUT method.
     * @param tsRow original row
     * @param entity edited row
     * @return edited row
     */
    private TimesheetRow tsBuilder(TimesheetRow tsRow, TimesheetRow entity) {
        entity.setProjectId(tsRow.getProjectId());
        entity.setWorkPackage(tsRow.getWorkPackage());
        entity.setNotes(tsRow.getNotes());
        entity.setSunHours(tsRow.getSunHours());
        entity.setMonHours(tsRow.getMonHours());
        entity.setTueHours(tsRow.getTueHours());
        entity.setWedHours(tsRow.getWedHours());
        entity.setThuHours(tsRow.getThuHours());
        entity.setFriHours(tsRow.getFriHours());
        entity.setSatHours(tsRow.getSatHours());
        entity.setTimesheetId(tsRow.getTimesheetId());
        entity.setTimesheetRowId(tsRow.getTimesheetRowId());
        return entity;
    }
    
    /**
     * Creates a new timesheet row.
     * @param token user token
     * @param payload timesheet row data
     * @return copy of new timesheet row
     */
    @Transactional
    @POST
    @Path("/row")
    @Consumes("application/json")
    @Produces("application/json")
    public Response createRow(@HeaderParam("token") String token, 
            String payload) {
        
        Auth auth = AuthenticationService.verifyToken(token);
        if (auth == null) {
            //failed to authenticate or user is not admin
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        String returnCode = "";
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        TimesheetRow tsRow = gson.fromJson(payload, TimesheetRow.class);
        
        em = Resource.getEntityManager();
        Timesheet timesheet = em.find(Timesheet.class, tsRow.getTimesheetId());
        
        if (timesheet == null) {
            returnCode = "{\"status\":\"400\"," + "\"message\":\"Resource"
                    + " not created.\"}";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(returnCode).build();
        }
        
        if (!auth.isAdmin() && auth.getEmpNumber() 
                != timesheet.getEmpNumber()) {
            //user is not admin, or does not own this timesheet
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            em.getTransaction().begin();
            em.persist(tsRow);
            em.flush();
            em.refresh(tsRow);
            em.getTransaction().commit();
            em.close();

            returnCode = tsRow.toString();
        } catch (IllegalArgumentException err) {
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
        String returnCode = "";
        em = Resource.getEntityManager();
        em.getTransaction().begin();
        
        Timesheet existingTimesheet = em.find(Timesheet.class, timesheetId);
        
        if (existingTimesheet == null) {
            //Timesheet does not exist in database
            returnCode = "{\"status\":\"404\"," + "\"message\":\"Resource"
                    + " not deleted.\"}";
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(returnCode).build();
        }
        
        if (!auth.isAdmin() && auth.getEmpNumber() 
                != existingTimesheet.getEmpNumber()) {
            //User does not have permission to delete this timehseet 
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        deleteRowsForTimesheet(timesheetId);
        em.remove(existingTimesheet);
        em.getTransaction().commit();
        em.close();
        returnCode = "{" + "\"message\":\"Timesheet succesfully"
                + " deleted\"" + "}";
        
        return Response.status(Response.Status.NO_CONTENT)
                .entity(returnCode).build();
    }
    
    /**
     * Deletes an timesheet row.
     * @param timesheetRowId identifies the row to delete 
     * @param token user token
     * @return Response code
     */
    @Transactional
    @DELETE
    @Consumes("application/json")
    @Produces("application/json")
    @Path("row/{rowId}")
    public Response deleteEmployee(@PathParam("rowId") int timesheetRowId, 
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
            TimesheetRow sheet = em.find(TimesheetRow.class, timesheetRowId);
            em.remove(sheet);
            em.getTransaction().commit();
            em.close();
            returnCode = "{" + "\"message\":\"Timesheet Row " 
                    + sheet.getTimesheetId() + "succesfully deleted\""
                    + "}";
        } catch (IllegalArgumentException err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"404\"," + "\"message\":\"Resource"
                    + " not deleted.\"" + "}";
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(returnCode).build();
        }
        return Response.status(Response.Status.NO_CONTENT)
                .entity(returnCode).build();
    }
    
    private void deleteRowsForTimesheet(int id) {
        Query query = em.createQuery(
                "DELETE FROM com.entity.TimesheetRow WHERE timesheet_id = :tid")
                .setParameter("tid", id);
        query.executeUpdate();
    }
}
