package edu.ucmo.cptonline.datasource;

/**
 * Created by avina on 5/1/2017.
 */

public class Logins {

    private long id;

    private String password;

    private String email;

    private int isadmin;

    private int isinternshipoffice;

    private String name;

    private int tempcode;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getIsadmin() {
        return isadmin;
    }

    public void setIsadmin(int isadmin) {
        this.isadmin = isadmin;
    }

    public int getIsinternshipoffice() {
        return isinternshipoffice;
    }

    public void setIsinternshipoffice(int isinternshipoffice) {
        this.isinternshipoffice = isinternshipoffice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTempcode() {
        return tempcode;
    }

    public void setTempcode(int tempcode) {
        this.tempcode = tempcode;
    }
}
