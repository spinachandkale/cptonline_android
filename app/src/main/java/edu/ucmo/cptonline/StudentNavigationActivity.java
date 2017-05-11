package edu.ucmo.cptonline;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

import edu.ucmo.cptonline.datasource.Applications;
import edu.ucmo.cptonline.datasource.Students;
import edu.ucmo.cptonline.helper.NetworkRequest;

public class StudentNavigationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_navigation);
        super.onCreateDrawer();

        Button btnFileApplication = (Button) findViewById(R.id.btnFileApplication);
        Button btnUploadFiles = (Button) findViewById(R.id.btnUploadFiles);
        TextView status = (TextView) findViewById(R.id.nav_status);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String email = preferences.getString("email", null);

        String[] emailParts = email.split("@");

        NetworkRequest nr = new NetworkRequest("http://35.188.97.91:8761/students/"+emailParts[0]);
        nr.getRequestDefault();
        nr.waitForResult();
        String response = nr.getResponse();

        ObjectMapper mapper = new ObjectMapper();
        Long studentId = Long.valueOf(0);
        try {
            Students[] students = mapper.readValue(response, Students[].class);
            if (students.length > 0)
                studentId = students[0].getId();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (studentId != 0) {
            nr.setURL("http://35.188.97.91:8761/applications/"+studentId);
            nr.setResponse("");
            nr.getRequestDefault();
            nr.waitForResult();
            response = nr.getResponse();

            try {
                Applications[] applications = mapper.readValue(response,Applications[].class);

                if (applications != null && !applications[0].getStatus().isEmpty()) {

                    String statusMessage = "";
                    switch (applications[0].getStatus()) {
                        case "student-submission-in-progress":
                            statusMessage = "Application is not submitted yet, you need to fill application and upload offer letter and central degree to complete the process";
                            break;
                        case "coordinator-verification-required":
                            statusMessage = "Your application is currently verified by CIS department coordinator, this has to go through internship office and graduation office as well";
                            break;
                        case "internship-office-verification":
                            statusMessage = "Your application is currently verified by internship department, this has to go through graduation office as well";
                            break;
                        case "submitted-to-international-center":
                            statusMessage = "Graduation office needs to verify your application";
                            break;
                    }
                    status.setText(statusMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            status.setText("Start filing your application");
        }

        if (studentId != 0) {
            saveToSharedPreferences("studentId", String.valueOf(studentId));
        }

        // Add action listeners for buttons

        btnFileApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToFileApplication();
            }
        });

        btnUploadFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToUploadfiles();
            }
        });
    }

    void proceedToFileApplication() {
        Intent intent = new Intent(this, studentInfoActivity.class);
        startActivity(intent);
    }

    void proceedToUploadfiles() {
        Intent intent = new Intent(this, UploadFilesActivity.class);
        startActivity(intent);
    }

    public void saveToSharedPreferences(String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }
}
