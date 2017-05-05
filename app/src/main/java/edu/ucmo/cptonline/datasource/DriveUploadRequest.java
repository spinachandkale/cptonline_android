package edu.ucmo.cptonline.datasource;

/**
 * Created by avina on 5/4/2017.
 */

public class DriveUploadRequest {
    private String studentId;
    private String filename;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
