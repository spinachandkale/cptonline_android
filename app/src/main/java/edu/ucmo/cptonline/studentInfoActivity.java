package edu.ucmo.cptonline;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import edu.ucmo.cptonline.datasource.Applications;
import edu.ucmo.cptonline.datasource.DriveShareRequest;
import edu.ucmo.cptonline.datasource.DriveUploadRequest;
import edu.ucmo.cptonline.datasource.Notifications;
import edu.ucmo.cptonline.datasource.Students;
import edu.ucmo.cptonline.helper.GoogleDriveHelper;
import edu.ucmo.cptonline.helper.NetworkRequest;
import edu.ucmo.cptonline.helper.PdfHelper;
import edu.ucmo.cptonline.helper.UploadHelper;

public class studentInfoActivity extends AppCompatActivity {

    private String studentEmail;
    private Boolean newApplication;
    private String pdfFile;
    private String directoryLink;
    private Students studentInDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info);

        Button edit = (Button) findViewById(R.id.btnEditStudentInfo);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((Button)v).getText() == "Edit") {
                    setupUI((View)findViewById(R.id.info_layout), true);
                    ((Button)v).setText("Save");
                } else if(((Button)v).getText() == "Save"){
                    saveStudentDetails();
                    proceedToNavigationActivity();
                }
            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        studentEmail= preferences.getString("email", null);

        String[] emailParts = studentEmail.split("@");

        NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/students/"+emailParts[0]);
        nr.getRequestDefault();
        nr.waitForResult();
        String response = nr.getResponse();

        ObjectMapper mapper = new ObjectMapper();
        Students[] students;
        try {
            students = mapper.readValue(response, Students[].class);
            newApplication = (students.length == 0) ? Boolean.TRUE : Boolean.FALSE;
            if (students.length > 0) {
                studentInDB = students[0];
                displayStudent(studentInDB);
                setupUI(findViewById(R.id.info_layout), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupUI(View view, boolean editable) {

        if (view instanceof EditText) {
            ((EditText)view).setFocusable(editable);
            return;
        }

        if (view instanceof RadioButton) {
            ((RadioButton)view).setFocusable(editable);
            return;
        }

        if (view instanceof Spinner) {
            ((Spinner)view).setClickable(editable);
            return;
        }

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView, editable);
            }
        }
    }


    private void displayStudent(Students student) {

        // Student Info

        ((EditText)findViewById(R.id.info_student_name)).setText(student.getName());

        ((EditText)findViewById(R.id.info_student_id)).setText(Long.toString(student.getId()));

        ((EditText)findViewById(R.id.info_student_major)).setText(student.getMajor());

        ((EditText)findViewById(R.id.info_student_gpa)).setText(student.getGpa());

        ((EditText)findViewById(R.id.info_student_gpa)).setText(student.getGpa());

        Spinner gradSemester = (Spinner)findViewById(R.id.info_student_gradsemester);
        gradSemester.setSelection(getSpinnerIndex(gradSemester, student.getGradsemester()));

        Spinner gradYear = (Spinner) findViewById(R.id.info_student_gradyear);
        gradYear.setSelection(getSpinnerIndex(gradYear, Integer.toString(student.getGradyear())));

        ((EditText)findViewById(R.id.info_student_addressline1)).setText(student.getStreet());

        ((EditText)findViewById(R.id.info_student_addressline2)).setText(student.getAptunitno());

        ((EditText)findViewById(R.id.info_student_citystatezip)).setText(student.getCitystatezip());
        ((EditText)findViewById(R.id.info_student_phone)).setText(student.getPhonenumber());

        RadioGroup rg = (RadioGroup) findViewById(R.id.info_student_ssn);
        if (student.getSsnrequired() == 1) {
            rg.check(R.id.info_student_ssn_no);
        } else if(student.getSsnrequired() == 0) {
            rg.check(R.id.info_student_ssn_yes);
        }

        rg = (RadioGroup) findViewById(R.id.info_student_twosemesters);
        if (student.getOneyearstudied() == 1 ) {
            rg.check(R.id.info_student_twosemester_yes);
        } else if(student.getOneyearstudied() == 0) {
            rg.check(R.id.info_student_twosemester_no);
        }

        // CPT type information

        rg = ((RadioGroup)findViewById(R.id.info_cpt_type));
        if (student.getCpttype().equals("Full time")) {
            rg.check(R.id.info_cpt_type_fulltime);
        } else {
            rg.check(R.id.info_cpt_type_parttime);
        }

        ((EditText)findViewById(R.id.info_cpt_instructor)).setText(student.getCptcourseinstructor());

        ((EditText)findViewById(R.id.info_cpt_credithours)).setText(student.getCptsemestercredithours());

        Spinner cptSemester = (Spinner)findViewById(R.id.info_cpt_semester);
        cptSemester.setSelection(getSpinnerIndex(gradSemester, student.getCptsemester()));

        Spinner cptYear = (Spinner) findViewById(R.id.info_cpt_year);
        cptYear.setSelection(getSpinnerIndex(gradYear, Integer.toString(student.getCptyear())));

        ((EditText)findViewById(R.id.info_cpt_startdate)).setText(student.getCptstart().toString());

        ((EditText)findViewById(R.id.info_cpt_enddate)).setText(student.getCptend().toString());

        ((EditText)findViewById(R.id.info_cpt_jobtitle)).setText(student.getCptjobtitle());

        ((EditText)findViewById(R.id.info_cpt_jobdescription)).setText(student.getCptjobdescription());

        // Company information

        ((EditText)findViewById(R.id.info_company_name)).setText(student.getCptemployer());

        ((EditText)findViewById(R.id.info_company_addressline)).setText(student.getCptemployerstreet());

        ((EditText)findViewById(R.id.info_company_citystatezip)).setText(student.getCptemployercitystatezip());

        ((EditText)findViewById(R.id.info_company_supervisornametitle)).setText(student.getCptsupervisornametitle());

        ((EditText)findViewById(R.id.info_company_supervisoremail)).setText(student.getCptsupervisoremail());

        ((EditText)findViewById(R.id.info_company_supervisorphone)).setText(student.getCptsupervisorphno());

    }

    private int getSpinnerIndex(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }


    private void saveStudentDetails() {

        Students student = studentInDB;

        // Fill student information

        student.setName(((EditText)findViewById(R.id.info_student_name)).getText().toString());

        student.setId(Long.parseLong(((EditText)findViewById(R.id.info_student_id)).getText().toString()));

        student.setMajor(((EditText)findViewById(R.id.info_student_major)).getText().toString());

        student.setGpa(((EditText)findViewById(R.id.info_student_gpa)).getText().toString());

        student.setGpa(((EditText)findViewById(R.id.info_student_gpa)).getText().toString());

        student.setGradsemester(((Spinner)findViewById(R.id.info_student_gradsemester)).getSelectedItem().toString());

        student.setGradyear(Integer.parseInt(((Spinner)findViewById(R.id.info_student_gradyear)).getSelectedItem().toString()));

        student.setStreet(((EditText)findViewById(R.id.info_student_addressline1)).getText().toString());

        student.setAptunitno(((EditText)findViewById(R.id.info_student_addressline2)).getText().toString());

        student.setCitystatezip(((EditText)findViewById(R.id.info_student_citystatezip)).getText().toString());

        student.setPhonenumber(((EditText)findViewById(R.id.info_student_phone)).getText().toString());

        switch (((RadioGroup)findViewById(R.id.info_student_ssn)).getCheckedRadioButtonId()) {
            case R.id.info_student_ssn_yes:
                // Question format is reverse, so setting in opposite way
                student.setSsnrequired(0);
                break;
            case R.id.info_student_ssn_no:
                student.setSsnrequired(1);
                break;
        }

        switch (((RadioGroup)findViewById(R.id.info_student_twosemesters)).getCheckedRadioButtonId()) {
            case R.id.info_student_twosemester_yes:
                student.setOneyearstudied(1);
                break;
            case R.id.info_student_twosemester_no:
                student.setOneyearstudied(0);
                break;
        }

        // Fill CPT department information

        switch (((RadioGroup)findViewById(R.id.info_cpt_type)).getCheckedRadioButtonId()) {
            case R.id.info_cpt_type_fulltime:
                student.setCpttype("Full time");
                break;
            case R.id.info_cpt_type_parttime:
                student.setCpttype("part time");
                break;
        }

        student.setCptcourseinstructor(((EditText)findViewById(R.id.info_cpt_instructor)).getText().toString());

        student.setCptsemestercredithours(Integer.parseInt(((EditText)findViewById(R.id.info_cpt_credithours)).getText().toString()));

        student.setCptsemester(((Spinner)findViewById(R.id.info_cpt_semester)).getSelectedItem().toString());

        student.setCptyear(Integer.parseInt(((Spinner)findViewById(R.id.info_cpt_year)).getSelectedItem().toString()));

        DateFormat formatter = new SimpleDateFormat("YYYY-mm-dd");
        try {
            Date date = formatter.parse(((EditText)findViewById(R.id.info_cpt_startdate)).getText().toString());
            student.setCptstart(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            Date date = formatter.parse(((EditText)findViewById(R.id.info_cpt_enddate)).getText().toString());
            student.setCptend(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        student.setCptjobtitle(((EditText)findViewById(R.id.info_cpt_jobtitle)).getText().toString());

        student.setCptjobdescription(((EditText)findViewById(R.id.info_cpt_jobdescription)).getText().toString());

        // Fill Company information

        student.setCptemployer(((EditText)findViewById(R.id.info_company_name)).getText().toString());

        student.setCptemployerstreet(((EditText)findViewById(R.id.info_company_addressline)).getText().toString());

        student.setCptemployercitystatezip(((EditText)findViewById(R.id.info_company_citystatezip)).getText().toString());

        student.setCptsupervisornametitle(((EditText)findViewById(R.id.info_company_supervisornametitle)).getText().toString());

        student.setCptsupervisoremail(((EditText)findViewById(R.id.info_company_supervisoremail)).getText().toString());

        student.setCptsupervisorphno(((EditText)findViewById(R.id.info_company_supervisorphone)).getText().toString());

        // create pdf and upload

        String filename  = Environment.getExternalStorageDirectory() + "/cptonline/" + "application_" + getDateTime();
        if (createPDF(student, filename)) {
            if (!uploadFile(filename, Long.toString(student.getId()))) {
                Toast.makeText(this, "unable to upload file to drive", Toast.LENGTH_LONG).show();
                return;
            }
        }
        if (student.getDirectorylink().isEmpty()) {
            student.setDirectorylink(directoryLink);
        }

        File file = new File(filename);
        student.setInternshipform(file.getName());

        // Send save request to service
        if (saveStudent(student)) {
            Toast.makeText(getApplicationContext(), "Unable to save student application, processing error", Toast.LENGTH_LONG).show();
        }
        // Save Application
        Date today = Calendar.getInstance().getTime();
        Applications application = new Applications();
        application.setStudentid(student.getId());
        application.setName(student.getName());
        application.setDateapplied(today);
        if (!student.getInternshipform().isEmpty() && !student.getOfferletter().isEmpty()
                && !student.getCentraldegree().isEmpty()
                && !student.getDirectorylink().isEmpty()) {
            if (shareDriveFolder()) {
                application.setStatus("coordinator-verification-required");
            } else {
                application.setStatus("student-submission-in-progress");
                Toast.makeText(this, "Unable to share folder in drive", Toast.LENGTH_LONG).show();
            }
        } else{
            application.setStatus("student-submission-in-progress");
        }
        if (saveApplication(application)) {
            Toast.makeText(this, "Application saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unable to save application", Toast.LENGTH_SHORT).show();
        }
    }

    public Boolean saveStudent(Students student) {
        NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/students/");
        if (newApplication) {
            nr.postStudent(student);
        } else {
            nr.putStudent(student);
        }
        nr.waitForResult();
        String response = nr.getResponse();
        return !(response.equals("error"));
    }

    public Boolean saveApplication(Applications application) {
        NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/applications/");
        if (newApplication) {
            nr.postApplication(application);
        } else {
            nr.putApplication(application);
        }
        nr.waitForResult();
        String response = nr.getResponse();
        return !(response.equals("error"));
    }

    private void proceedToNavigationActivity() {
        Intent intent = new Intent(this, StudentNavigationActivity.class);
        startActivity(intent);
    }

    private Boolean createPDF(Students student, String filename) {
        PdfHelper pdfHelper = new PdfHelper(student);
        return pdfHelper.createPdf(filename);
    }
//
//    private void uploadPDF(String pdffile, Long studentId) {
//        InputStream in = getResources().openRawResource(getResources().getIdentifier("client_secret",
//                "raw", getPackageName()));
//        GoogleDriveHelper driveHelper = new GoogleDriveHelper(in);
//        try {
//            driveHelper.uploadFile(pdffile, Long.toString(studentId));
//            directoryLink = driveHelper.getDirectoryLink();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private Boolean uploadFile(String filename, String studentId) {
        Boolean ret = Boolean.FALSE;
//        UploadHelper uh = new UploadHelper("http://35.188.97.91/cptuploads/postfile.php", filename, "application/pdf");
        ArrayList<String> args = new ArrayList<>();
        args.add("http://35.188.97.91/cptuploads/postfile.php");
        args.add(filename);
        args.add("application/pdf");
        UploadHelper fileTransfer = new UploadHelper();
        fileTransfer.execute(args);
        while(fileTransfer.getStatus() != AsyncTask.Status.FINISHED) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(fileTransfer.getTaskResult() == 1) {
            DriveUploadRequest request = new DriveUploadRequest();
            File file = new File(filename);
            request.setFilename(file.getName());
            request.setStudentId(studentId);

            NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8759/uploads");
            nr.postDriveUpload(request);
            nr.waitForResult();
            String response = nr.getResponse();
            if(response.equals("")) {
                Toast.makeText(this, "Problems in uploading to drive", Toast.LENGTH_SHORT).show();
            } else {
                directoryLink = response;
                ret = Boolean.TRUE;
            }
        }
        return  ret;
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private Boolean shareDriveFolder() {
        DriveShareRequest request = new DriveShareRequest();
        request.setEmail(getNotificationsEmail("coordinator-verification-required"));
        request.setFileID(directoryLink);
        request.setStudentID(getSharedPreferenceValue("studentId"));

        NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8759/uploads/share/");
        nr.postDriveShare(request);
        nr.waitForResult();
        String response = nr.getResponse();
        if (response.equals("success")) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    private String getNotificationsEmail(String status) {
        NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/notifications/"+status);
        nr.getRequestDefault();
        nr.waitForResult();
        String response = nr.getResponse();

        ObjectMapper mapper = new ObjectMapper();
        Notifications notification = null;
        try {
            notification = mapper.readValue(response, Notifications.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (notification.getEmail() != null) ? notification.getEmail() : "";
    }

    private String getSharedPreferenceValue(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return preferences.getString(key, null);
    }

}
