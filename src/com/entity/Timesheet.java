package com.entity;

import java.math.BigDecimal;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A class representing a single Timesheet.
 *
 * @author Tony
 * @version 2
 */
@XmlRootElement
@Entity
@Table(name = "timesheet")
public class Timesheet {
    
    /** ID number for employee who owns timesheet. */
    @Column(name = "emp_number")
    private int empNumber;
    
    /** The date of Friday for the week of the timesheet. */
    @Column(name = "end_week")
    private Date endWeek;
    
    /** The total number of overtime hours on the timesheet. */
    @Column(name = "overtime")
    private BigDecimal overtime;
    
    /** The total number of flextime hours on the timesheet. */
    @Column(name = "flextime")
    private BigDecimal flextime;

    /** ID number for timesheet. */
    @Id
    @Column(name = "timesheet_id")
    private int timesheetId;

    /**
     * Default Constructor for JPA.
     */
    public Timesheet() {
    }
    
    /**
     * Creates a Timesheet object with all fields set. 
     * 
     * @param id Timesheet id
     * @param empNum The owner of the timesheet
     * @param endWeek The date of the end of the week for the 
     * timesheet
     */
    public Timesheet(int id, int empNum, Date endWeek) {
        timesheetId = id;
        empNumber = empNum;
        this.endWeek = endWeek;
        flextime = new BigDecimal(0);
        overtime = new BigDecimal(0);
    }
    
    /**
     * Getter for time sheet owner.
     * @return the employee id.
     */
    public int getEmpNumber() {
        return empNumber;
    }

    /**
     * Setter for time sheet owner.
     * @param empNumber id of employee
     */
    public void setEmpNumber(int empNumber) {
        this.empNumber = empNumber;
    }

    /**
     * Getter for timesheet's end of week date.
     * @return the endWeek
     */
    public Date getEndWeek() {
        return endWeek;
    }

    /**
     * Setter for endWeek.
     * @param endWeek date of the end week
     */
    public void setEndWeek(Date endWeek) {
        this.endWeek = endWeek;
    }

    /**
     * Getter for overtime.
     * @return OT hours
     */
    public BigDecimal getOvertime() {
        return overtime;
    }

    /**
     * Setter for overtime.
     * @param overtime OT hours
     */
    public void setOvertime(BigDecimal overtime) {
        this.overtime = overtime;
    }

    /**
     * Getter for flex hours.
     * @return flex hours
     */
    public BigDecimal getFlextime() {
        return flextime;
    }

    /**
     * Setter for flex hours.
     * @param flextime hours
     */
    public void setFlextime(BigDecimal flextime) {
        this.flextime = flextime;
    }

    /**
     * Getter for timesheet's id.
     * @return id to timesheet
     */
    public int getTimesheetId() {
        return timesheetId;
    }
    
    /**
     * Setter for timesheet id.
     * @param timesheetId id to be set
     */
    public void setTimesheetId(int timesheetId) {
        this.timesheetId = timesheetId;
    }
    
    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        String json = gson.toJson(this);  
        return json + "\n";
    }
    
}
