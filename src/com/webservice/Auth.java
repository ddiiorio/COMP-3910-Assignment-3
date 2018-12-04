package com.webservice;

/**
 * A class representing login authorization data.
 *
 * @author Tony
 * @version 1
 */
public class Auth {
    /** Employee number. */
    private int empNumber;
    /** Employee admin status. */
    private boolean isAdmin;
    
    /**
     * Constructor.
     * @param empNumber employee number
     * @param isAdmin admin status
     */
    public Auth(int empNumber, boolean isAdmin) {
        this.empNumber = empNumber;
        this.isAdmin = isAdmin;
    }
    
    /**
     * Getter for employee number.
     * @return number
     */
    public int getEmpNumber() {
        return empNumber;
    }
    
    /**
     * Getter for admin status.
     * @return true if admin
     */
    public boolean isAdmin() {
        return isAdmin;
    }
}
