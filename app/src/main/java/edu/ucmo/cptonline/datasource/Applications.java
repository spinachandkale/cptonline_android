package edu.ucmo.cptonline.datasource;

import java.util.Date;

/**
 * Created by avina on 5/1/2017.
 */

public class Applications {

    private Long id;

    private Long studentid;

    private String name;

    private Date dateapplied;

    private String status;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentid() {
        return studentid;
    }

    public void setStudentid(Long studentid) {
        this.studentid = studentid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateapplied() {
        return dateapplied;
    }

    public void setDateapplied(Date dateapplied) {
        this.dateapplied = dateapplied;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
