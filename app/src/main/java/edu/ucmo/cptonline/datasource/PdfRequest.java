package edu.ucmo.cptonline.datasource;

/**
 * Created by avina on 5/5/2017.
 */

public class PdfRequest {

    String studentId;
    String htmlContent;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }
}
