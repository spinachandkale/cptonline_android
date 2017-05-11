package edu.ucmo.cptonline;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.drm.ProcessedData;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.KeyListener;
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

public class studentInfoActivity extends BaseActivity {

    private String studentEmail;
    private Boolean newApplication;
    private String pdfFile;
    private String directoryLink;
    private Students studentInDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info);
        super.onCreateDrawer();

        Button edit = (Button) findViewById(R.id.btnEditStudentInfo);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((Button)v).getText().equals("Edit")) {
                    setupUI((View)findViewById(R.id.info_layout), true);
                    ((Button)v).setText("Save");
                } else if(((Button)v).getText().equals("Save")){
                    ProgressDialog dialog = new ProgressDialog(studentInfoActivity.this);
                    dialog.setMessage("Saving details");
                    dialog.show();
                    Students student = getStudentDetails();
                    saveTask sTask = new saveTask(student, dialog);
                    sTask.execute((Void)null);

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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.

        final View mRegisterFormView = findViewById(R.id.info_form);
        final View mProgressView = findViewById(R.id.info_progress);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void setupUI(View view, boolean editable) {

        if (view instanceof EditText) {
//            ((EditText)view).setFocusable(editable);
//            ((EditText)view).setEnabled(editable);
            if (editable) {
                ((EditText)view).setKeyListener((KeyListener) view.getTag());
            } else {
                view.setTag(((EditText)view).getKeyListener());
                ((EditText)view).setKeyListener(null);
            }
            return;
        }

        if (view instanceof RadioButton) {
            ((RadioButton)view).setFocusable(editable);
            ((RadioButton)view).setEnabled(editable);
            return;
        }

        if (view instanceof Spinner) {
            ((Spinner)view).setClickable(editable);
            ((Spinner)view).setEnabled(editable);

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

        ((EditText)findViewById(R.id.info_student_name)).setText((student.getName() != null) ? student.getName() : "");

        ((EditText)findViewById(R.id.info_student_id)).setText(Long.toString(student.getId()));

        ((EditText)findViewById(R.id.info_student_major)).setText((student.getMajor() != null) ? student.getMajor() : "");

        ((EditText)findViewById(R.id.info_student_gpa)).setText((student.getGpa() != null) ? student.getGpa() : "");

        ((EditText)findViewById(R.id.info_student_gpa)).setText((student.getGpa() != null) ? student.getGpa() : "");

        if (student.getGradsemester() != null) {
            Spinner gradSemester = (Spinner) findViewById(R.id.info_student_gradsemester);
            gradSemester.setSelection(getSpinnerIndex(gradSemester, student.getGradsemester()));
        }
        Spinner gradYear = (Spinner) findViewById(R.id.info_student_gradyear);
        gradYear.setSelection(getSpinnerIndex(gradYear, Integer.toString(student.getGradyear())));

        ((EditText)findViewById(R.id.info_student_addressline1)).setText((student.getStreet() != null) ? student.getStreet() : "");

        ((EditText)findViewById(R.id.info_student_addressline2)).setText((student.getAptunitno() != null) ? student.getAptunitno() : "");

        ((EditText)findViewById(R.id.info_student_citystatezip)).setText((student.getCitystatezip() != null) ? student.getCitystatezip() : "");
        ((EditText)findViewById(R.id.info_student_phone)).setText((student.getPhonenumber() != null) ? student.getPhonenumber() : "");

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
        if (student.getCpttype() != null && student.getCpttype().equals("Full time")) {
            rg.check(R.id.info_cpt_type_fulltime);
        } else if (student.getCpttype() != null && student.getCpttype().equals("Part time")){
            rg.check(R.id.info_cpt_type_parttime);
        }

        ((EditText)findViewById(R.id.info_cpt_instructor)).setText((student.getCptcourseinstructor() != null)
                ? student.getCptcourseinstructor() : "");

        ((EditText)findViewById(R.id.info_cpt_credithours)).setText(Integer.toString(student.getCptsemestercredithours()));

        if (student.getCptsemester() != null) {
            Spinner cptSemester = (Spinner) findViewById(R.id.info_cpt_semester);
            cptSemester.setSelection(getSpinnerIndex(cptSemester, student.getCptsemester()));
        }
        Spinner cptYear = (Spinner) findViewById(R.id.info_cpt_year);
        cptYear.setSelection(getSpinnerIndex(cptYear, Integer.toString(student.getCptyear())));

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        ((EditText)findViewById(R.id.info_cpt_startdate)).setText((student.getCptstart() != null)
                ? df.format(student.getCptstart()) : "0000-00-00");

        ((EditText)findViewById(R.id.info_cpt_enddate)).setText((student.getCptend() != null)
                ? df.format(student.getCptend()) : "0000-00-00");

        ((EditText)findViewById(R.id.info_cpt_jobtitle)).setText((student.getCptjobtitle() != null)
                ? student.getCptjobtitle() : "");

        ((EditText)findViewById(R.id.info_cpt_jobdescription)).setText((student.getCptjobdescription() != null)
                ? student.getCptjobdescription() : "");

        // Company information

        ((EditText)findViewById(R.id.info_company_name)).setText((student.getCptemployer() != null)
                ? student.getCptemployer() : "");

        ((EditText)findViewById(R.id.info_company_addressline)).setText((student.getCptemployerstreet() != null)
                ? student.getCptemployerstreet() : "");

        ((EditText)findViewById(R.id.info_company_citystatezip)).setText((student.getCptemployercitystatezip() != null) ?
                student.getCptemployercitystatezip() : "");

        ((EditText)findViewById(R.id.info_company_supervisornametitle)).setText((student.getCptsupervisornametitle() != null)
                ? student.getCptsupervisornametitle() : "");

        ((EditText)findViewById(R.id.info_company_supervisoremail)).setText((student.getCptsupervisoremail() != null)
                ? student.getCptsupervisoremail() : "");

        ((EditText)findViewById(R.id.info_company_supervisorphone)).setText((student.getCptsupervisorphno() != null)
                ? student.getCptsupervisorphno() : "");

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

    private Students getStudentDetails() {
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
                student.setCpttype("Part time");
                break;
        }

        student.setCptcourseinstructor(((EditText)findViewById(R.id.info_cpt_instructor)).getText().toString());

        student.setCptsemestercredithours(Integer.parseInt(((EditText)findViewById(R.id.info_cpt_credithours)).getText().toString()));

        student.setCptsemester(((Spinner)findViewById(R.id.info_cpt_semester)).getSelectedItem().toString());

        student.setCptyear(Integer.parseInt(((Spinner)findViewById(R.id.info_cpt_year)).getSelectedItem().toString()));

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
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

        return student;

    }

    public class saveTask extends AsyncTask<Void, Void, Boolean> {

        private Students student;
        private ProgressDialog pd;

        public saveTask(Students parsedStudent, ProgressDialog pd) {
            this.student  = parsedStudent;
            this.pd = pd;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return saveStudentDetails(student);
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            pd.dismiss();
            if (!result) {
                Toast.makeText(getApplicationContext(), "Unable to save student details", Toast.LENGTH_LONG);
            }
            proceedToNavigationActivity();
        }



        private Boolean saveStudentDetails(Students student) {


            directoryLink = createPDF(student);

            if (!directoryLink.isEmpty() && student.getDirectorylink() != null
                    && student.getDirectorylink().isEmpty()) {
                student.setDirectorylink(directoryLink);
            }

            student.setInternshipform(directoryLink);

            // Send save request to service
            if (!saveStudent(student)) {
                return Boolean.FALSE;
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
                    return Boolean.FALSE;
                }
            } else{
                application.setStatus("student-submission-in-progress");
            }
            if (saveApplication(application)) {
            } else {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }


        private String createPDF(Students student) {
            PdfHelper pdfHelper = new PdfHelper(student);
            return pdfHelper.createPdf();
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
                Applications existingApplication = getApplication(Long.toString(application.getStudentid()));
                if (existingApplication != null) {
                    application.setId(existingApplication.getId());
                }
                nr.putApplication(application);
            }
            nr.waitForResult();
            String response = nr.getResponse();
            return !(response.equals("error"));
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

        private Applications getApplication(String studentId) {
            NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/applications/"+studentId);
            nr.getRequestDefault();
            nr.waitForResult();
            String response = nr.getResponse();

            ObjectMapper mapper = new ObjectMapper();
            Applications[] applications;
            try {
                applications = mapper.readValue(response, Applications[].class);
                if (applications.length > 0) {
                    return applications[0];
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String getSharedPreferenceValue(String key) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            return preferences.getString(key, null);
        }

    }

    private void proceedToNavigationActivity() {
        Intent intent = new Intent(this, StudentNavigationActivity.class);
        startActivity(intent);
        finish();
    }
}
