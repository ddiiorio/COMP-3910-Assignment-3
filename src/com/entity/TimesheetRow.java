package com.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A class representing a single TimesheetRow.
 *
 * @author Tony
 * @version 3
 */
@XmlRootElement
@Entity
@Table(name = "timesheet_row")
public class TimesheetRow implements Serializable {
    
    /** The projectID. */
    @Column(name = "project_id")
    private int projectId;
    
    /** The WorkPackage. Must be a unique for a given projectID. */
    @Column(name = "work_package")
    private String workPackage;
    
    /** Any notes added to the end of a row. */
    @Column(name = "notes")
    private String notes;
    
    /** The hours for Sunday. */
    @Column(name = "sun_hours")
    private BigDecimal sunHours;
    
    /** The hours for Monday. */
    @Column(name = "mon_hours")
    private BigDecimal monHours;
    
    /** The hours for Tuesday. */
    @Column(name = "tue_hours")
    private BigDecimal tueHours;    
    
    /** The hours for Wednesday. */
    @Column(name = "wed_hours")
    private BigDecimal wedHours;    
    
    /** The hours for Thursday. */
    @Column(name = "thu_hours")
    private BigDecimal thuHours;    
    
    /** The hours for Friday. */
    @Column(name = "fri_hours")
    private BigDecimal friHours;    
    
    /** The hours for Saturday. */
    @Column(name = "sat_hours")
    private BigDecimal satHours;
    
    /** The timesheet ID the row is in. */
    @Column(name = "timesheet_id")
    private int timesheetId;
    
    /** The row ID. */
    @Id
    @Column(name = "timesheet_row_id")
    private int timesheetRowId;

    /**
     * Default constructor for JPA.
     */
    public TimesheetRow() { }
    
    /**
     * Creates an empty timesheet row.
     * @param sheetid id for timesheet
     */
    public TimesheetRow(int sheetid) {
        timesheetId = sheetid;
        workPackage = "";
        sunHours = new BigDecimal(0);
        monHours = new BigDecimal(0);
        tueHours = new BigDecimal(0);
        wedHours = new BigDecimal(0);
        thuHours = new BigDecimal(0);
        friHours = new BigDecimal(0);
        satHours = new BigDecimal(0);
    }
    
    /**
     * projectID getter.
     * @return the projectID
     */
    public int getProjectId() {
        return projectId;
    }

    /**
     * projectID setter.
     * @param projectId the projectID to set
     */
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    /**
     * workPackage getter.
     * @return the workPackage
     */
    public String getWorkPackage() {
        return workPackage;
    }

    /**
     * workPackage setter.
     * @param workPackage the workPackage to set
     */
    public void setWorkPackage(String workPackage) {
        this.workPackage = workPackage;
    }

    /**
     * Getter for notes section.
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Setter for notes section.
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Getter for Sunday hours.
     * @return sunday hours
     */
    public BigDecimal getSunHours() {
        return sunHours;
    }

    /**
     * sunHours setter.
     * @param sunHours hours to set
     */
    public void setSunHours(BigDecimal sunHours) {
        this.sunHours = sunHours != null ? sunHours : BigDecimal.ZERO;
    }

    /**
     * Getter for Monday hours.
     * @return monday hours
     */
    public BigDecimal getMonHours() {
        return monHours;
    }

    /**
     * monHours setter.
     * @param monHours hours to set
     */
    public void setMonHours(BigDecimal monHours) {
        this.monHours = monHours != null ? monHours : BigDecimal.ZERO;
    }

    /**
     * Getter for Tuesday hours.
     * @return tuesday hours
     */
    public BigDecimal getTueHours() {
        return tueHours;
    }

    /**
     * tueHours setter.
     * @param tueHours hours to set
     */
    public void setTueHours(BigDecimal tueHours) {
        this.tueHours = tueHours != null ? tueHours : BigDecimal.ZERO;
    }

    /**
     * Getter for Wednesday hours.
     * @return wednesday hours
     */
    public BigDecimal getWedHours() {
        return wedHours;
    }

    /**
     * wedHours setter.
     * @param wedHours hours to set
     */
    public void setWedHours(BigDecimal wedHours) {
        this.wedHours = wedHours != null ? wedHours : BigDecimal.ZERO;
    }

    /**
     * Getter for Thursday hours.
     * @return thursday hours
     */
    public BigDecimal getThuHours() {
        return thuHours;
    }

    /**
     * thuHours setter.
     * @param thuHours hours to set
     */
    public void setThuHours(BigDecimal thuHours) {
        this.thuHours = thuHours != null ? thuHours : BigDecimal.ZERO;
    }

    /**
     * Getter for Friday hours.
     * @return friday hours
     */
    public BigDecimal getFriHours() {
        return friHours;
    }

    /**
     * friHours setter.
     * @param friHours hours to set
     */
    public void setFriHours(BigDecimal friHours) {
        this.friHours = friHours != null ? friHours : BigDecimal.ZERO;
    }

    /**
     * Getter for Saturday hours.
     * @return saturday hours
     */
    public BigDecimal getSatHours() {
        return satHours;
    }

    /**
     * satHours setter.
     * @param satHours hours to set
     */
    public void setSatHours(BigDecimal satHours) {
        this.satHours = satHours != null ? satHours : BigDecimal.ZERO;
    }

    /**
     * Getter for timesheet id.
     * @return timesheet's id
     */
    public int getTimesheetId() {
        return timesheetId;
    }

    /**
     * timesheetId setter.
     * @param timesheetId id to set
     */
    public void setTimesheetId(int timesheetId) {
        this.timesheetId = timesheetId;
    }

    /**
     * Getter for row id.
     * @return row id
     */
    public int getTimesheetRowId() {
        return timesheetRowId;
    }

    /**
     * timesheetRowId setter.
     * @param timesheetRowId id to set
     */
    public void setTimesheetRowId(int timesheetRowId) {
        this.timesheetRowId = timesheetRowId;
    }
    
    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        String json = gson.toJson(this);  
        return json + "\n";
    }
}
