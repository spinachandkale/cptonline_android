package edu.ucmo.cptonline.helper;

import android.content.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.ucmo.cptonline.datasource.Students;

/**
 * Created by avina on 5/2/2017.
 */

public class PdfHelper {

    String htmlContent;
    Students student;

    public PdfHelper(Students student) {

        this.student = student;

        htmlContent = "<body>\n" +
                "<p align=\"center\">\n" +
                "    <strong>MSIT INTERNSHIP FOR GRADUATE CREDIT</strong>\n" +
                "</p>\n" +
                "<p align=\"center\">\n" +
                "    <strong> </strong>\n" +
                "</p>\n" +
                "<p>\n" +
                "    <strong><i>STUDENT INFORMATION</i></strong>\n" +
                "</p>\n" +
                "<p>\n" +
                "    Student Name: <u>       @!NAME!@       </u>\n" +
                "</p>\n" +
                "<p>\n" +
                "    700#: <u>       @!ID!@      </u>\n" +
                "</p>\n" +
                "<p>\n" +
                "    Major: <u>    @!MAJOR!@     </u> GPA: <u>   @!GPA!@  </u>\n" +
                "</p>\n" +
                "<p>\n" +
                "    Anticipated Graduation: <u>   @!GRADSEM!@, @!GRADYEAR!@  </u>\n" +
                "</p>\n" +
                "<p>\n" +
                "    Address: <u> @!STUADDRESS!@, @!STUAPTNO!@, @!STUCITYSTATEZIP!@ </u>\n" +
                "</p>\n" +
                "<p>\n" +
                "    Student phone Number: <u> @!STUPHONE!@ </u> UCM Email Address: <u> @!STUEMAIL!@ </u>\n" +
                "</p>\n" +
                "<p>\n" +
                "    <em>*</em>\n" +
                "    <em>Do you have a Social Security Number:  @!STUSSN_Y_OR_N!@</em>\n" +
                "</p>\n" +
                "<p>\n" +
                "    <em>*</em>\n" +
                "    <em>Will you be completing 2 semesters (excluding Summer) before start of internship: @!STU2SEMS_Y_OR_N!@</em>\n" +
                "</p>\n" +
                "\n" +
                "<p>\n" +
                "    <strong><i>CPT/DEPARTMENT INFORMATION</i></strong>\n" +
                "</p>\n" +
                "<p>\n" +
                "    Type of CPT: <u>  @!CPTYPE!@ </u>\n" +
                "</p>\n" +
                "<p>\n" +
                "    Course Instructor/Academic Objective Evaluator: <u> @!INSTRUCTOR!@ </u>\n" +
                "</p>\n" +
                "\n" +
                "<p>\n" +
                "    Number of Credit Hours you will be enrolled in while in CPT: <u> @!CREDITHOURS!@ </u>\n" +
                "</p>\n" +
                "<p>\n" +
                "\t<em>\n" +
                "\t\tNote: Following details should be as per Offer letter \n" +
                "\t</em>\n" +
                "</p>\n" +
                "<p> \n" +
                "\tCPT Enrollment Semester (semester/year): <u> @!CPTSEMISTER!@, @!CPTYEAR!@ </u>\n" +
                "</p>\n" +
                "<p>\n" +
                "    CPT Employment Start Date (As per offer letter and Should be atleast 7 days from today):  <u> @!CPTSTARTDATE!@ </u> \n" +
                "</p>\n" +
                "<p>\n" +
                "    CPT Employment End/Last Date: <u> @!CPTENDDATE!@ </u>\n" +
                "</p>\n" +
                "<p>\n" +
                "    Job Title: <u> @!JOBTITLE!@  </u> Job description: <u> @!JOBDESCRIPTION!@ </u>\n" +
                "</p>\n" +
                "<p>\n" +
                "    <strong><i>COMPANY INFORMATION</i></strong>\n" +
                "</p>\n" +
                "<p>\n" +
                "\t  Name of Company where internship is to be performed: <u> @!COMPANYNAME!@ </u>\n" +
                "</p>\n" +
                "<p>\n" +
                "\t  CPT Company/Employer Street Address:  <u> @!COMPANYSTREET!@, @!COMPANYCITYSTATEZIP!@ </u>\n" +
                "</p>\n" +
                "<p>\n" +
                "    Supervisor Name and title:  <u> @!SUPERVISORNAMETITLE!@</u>  \n" +
                "</p>\n" +
                "<p>\n" +
                "    Supervisor Email address: <u> @!SUPERVISOREMAIL!@ </u> Supervisor phone: <u> @!SUPERVISORPHONE!@ </u>\n" +
                "</p>\n" +
                "<p>\n" +
                "    Date: <u>@!TODAYDATE!@ </u>\n" +
                "</p>\n" +
                "<p>\n" +
                "    <strong>SIGNATURES</strong>\n" +
                "</p>\n" +
                "<p>\n" +
                "    <em>*I acknowledge that the information provided is true and accurate to my knowledge.</em>\n" +
                "\t<br></br>\n" +
                "\t<em>*This application is a request for CPT authorization</em>\n" +
                "</p>\n" +
                "<p>\n" +
                "    <em>Signature: </em>\n" +
                "    ______________<em></em>\n" +
                "</p>\n" +
                "<p> \n" +
                "\t_______________________________________________________________________________________________________\n" +
                "\t\n" +
                "</p>\n" +
                "\n" +
                "</body>";

    }

    public String createPdf() {
        String filename = "application" + getDateTime();
        updateHtmlContent();
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filename);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(htmlContent, "/");
            builder.toStream(outputStream);
            builder.run();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filename;
    }

    private void updateHtmlContent() {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String htmlStr = htmlContent;

        htmlStr = htmlStr.replace("@!NAME!@",student.getName());
        htmlStr = htmlStr.replace("@!ID!@",Long.toString(student.getId()));
        htmlStr = htmlStr.replace("@!MAJOR!@",student.getMajor());
        htmlStr = htmlStr.replace("@!GPA!@",student.getGpa());
        htmlStr = htmlStr.replace("@!GRADSEM!@",student.getGradsemester());
        htmlStr = htmlStr.replace("@!GRADYEAR!@",Integer.toString(student.getGradyear()));
        htmlStr = htmlStr.replace("@!STUADDRESS!@",student.getStreet());
        htmlStr = htmlStr.replace("@!STUAPTNO!@",student.getAptunitno());
        htmlStr = htmlStr.replace("@!STUCITYSTATEZIP!@",student.getCitystatezip());
        htmlStr = htmlStr.replace("@!STUPHONE!@",student.getPhonenumber());
        htmlStr = htmlStr.replace("@!STUEMAIL!@",student.getEmail());
        htmlStr = htmlStr.replace("@!STUSSN_Y_OR_N!@",(student.getSsnrequired() == 1) ? "no" : "yes");
        htmlStr = htmlStr.replace("@!STU2SEMS_Y_OR_N!@",(student.getOneyearstudied() == 1) ? "yes" : "no");
        htmlStr = htmlStr.replace("@!CPTYPE!@",student.getCpttype());
        htmlStr = htmlStr.replace("@!INSTRUCTOR!@",student.getCptcourseinstructor());
        htmlStr = htmlStr.replace("@!CREDITHOURS!@",Integer.toString(student.getCptsemestercredithours()));
        htmlStr = htmlStr.replace("@!CPTSEMISTER!@",student.getCptsemester());
        htmlStr = htmlStr.replace("@!CPTYEAR!@",Integer.toString(student.getCptyear()));
        htmlStr = htmlStr.replace("@!CPTSTARTDATE!@",dateFormat.format(student.getCptstart()));
        htmlStr = htmlStr.replace("@!CPTENDDATE!@",dateFormat.format(student.getCptend()));
        htmlStr = htmlStr.replace("@!JOBTITLE!@",student.getCptjobtitle());
        htmlStr = htmlStr.replace("@!JOBDESCRIPTION!@",student.getCptjobdescription());
        htmlStr = htmlStr.replace("@!COMPANYNAME!@",student.getCptemployer());
        htmlStr = htmlStr.replace("@!COMPANYSTREET!@",student.getCptemployerstreet());
        htmlStr = htmlStr.replace("@!COMPANYCITYSTATEZIP!@",student.getCptemployercitystatezip());
        htmlStr = htmlStr.replace("@!SUPERVISORNAMETITLE!@",student.getCptsupervisornametitle());
        htmlStr = htmlStr.replace("@!SUPERVISOREMAIL!@",student.getCptsupervisoremail());
        htmlStr = htmlStr.replace(" @!SUPERVISORPHONE!@",student.getCptsupervisorphno());

        htmlContent = htmlStr;
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
