package com.webservice;

public class Auth {
    private int empNumber;
    private boolean isAdmin;
    public Auth(int empNumber, boolean isAdmin) {
        this.empNumber = empNumber;
        this.isAdmin = isAdmin;
    }
    public int getEmpNumber() {
        return empNumber;
    }
    public boolean isAdmin() {
        return isAdmin;
    }
}
