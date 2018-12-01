package com.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A class representing a single Employee.
 *
 * @author Tony
 * @version 3
 */
@XmlRootElement
@Entity
@Table(name = "employees")
public class Employees implements Serializable {

    /** The employee's last name. */
    @Column(name = "lastName")
    private String lastName;
    
    /** The employee's first name. */
    @Column(name = "firstName")
    private String firstName;
    
    /** The employee's login ID. */
    @Column(name = "userName")
    private String userName;
    
    /** The employee's employee number. */
    @Id
    @Column(name = "emp_number")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int empNumber;
    
    /** The employee's password. */
    @Column(name = "password")
    private String password;
    
    /** The employee's admin status. */
    @Column(name = "isAdmin")
    private boolean isAdmin;

    /**
     * Admin getter.
     * @return true if employee is an admin
     */
    public boolean getIsAdmin() {
        return isAdmin;
    }

    /**
     * Admin setter.
     * @param isAdmin boolean
     */
    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
    
    /**
     * last name getter.
     * 
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * last name setter.
     * 
     * @param lastName the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * first name getter.
     * 
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * first name setter.
     * 
     * @param firstName the first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * userName getter.
     * 
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * userName setter.
     * 
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * empNumber getter.
     * @return employee number
     */
    public int getEmpNumber() {
        return empNumber;
    }

    /**
     * empNumber setter.
     * @param empNumber employee number
     */
    public void setEmpNumber(int empNumber) {
        this.empNumber = empNumber;
    }

    /**
     * password getter.
     * @return employee's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * password setter.
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        String json = gson.toJson(this);  
        return json + "\n";
    }
}
