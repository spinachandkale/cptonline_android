package edu.ucmo.cptonline.datasource;

/**
 * Created by avina on 5/4/2017.
 */

public class DriveShareRequest {

    private String studentID;
    private String fileID;
    private String email;

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
